package org.nrg.xnatx.plugins.pixi.hotelsplitter.eventservice.actions;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.eventservice.actions.SingleActionProvider;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.events.ProjectEvent;
import org.nrg.xnat.eventservice.model.Action;
import org.nrg.xnat.eventservice.model.ActionAttributeConfiguration;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.services.SubscriptionDeliveryEntityService;
import org.nrg.xnat.utils.WorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.nrg.xft.event.XftItemEventI.CREATE;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_COMPLETE;
import static org.nrg.xnat.eventservice.entities.TimedEventStatusEntity.Status.ACTION_FAILED;

@Service
@Slf4j
public class PixiHotelSubjectAction extends SingleActionProvider {

    private final String displayName = "PIXI Create Hotel Subject Action";
    private final String description = "Creates a 'Hotel' subject for a project. The Hotel subject is used to store multi-subject 'hotel' images sessions which are later split into individual subject sessions.";

    private final String HOTEL_SUBJECT_LABEL = "Hotel";

    private final SubscriptionDeliveryEntityService subscriptionDeliveryEntityService;

    @Autowired
    public PixiHotelSubjectAction(SubscriptionDeliveryEntityService subscriptionDeliveryEntityService) {
        this.subscriptionDeliveryEntityService = subscriptionDeliveryEntityService;
    }

    @Override
    public Map<String, ActionAttributeConfiguration> getAttributes(String projectId, UserI user) {
        return new HashMap<>();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void processEvent(EventServiceEvent event, Subscription subscription, UserI user, Long deliveryId) {
        if (event.getStatiStates().contains(ProjectEvent.Status.CREATED.name())) {
            final String projectId = event.getProjectId();
            final String subjectLabel = HOTEL_SUBJECT_LABEL;

            final String subjectId;
            try {
                subjectId = XnatSubjectdata.CreateNewID();
            } catch (Exception e) {
                log.error("Unable to create 'Hotel' subject for project " + projectId, e);
                subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_FAILED, new Date(), "Unable to create 'Hotel' subject for project " + projectId);
                return;
            }

            log.info("Creating 'Hotel' subject in project {} with ID {}", projectId, subjectId);

            final XnatSubjectdata subject = new XnatSubjectdata(user);
            subject.setId(subjectId);
            subject.setProject(projectId);
            subject.setLabel(subjectLabel);

            final PersistentWorkflowI workflow;
            final EventMetaI eventMeta;

            try {
                workflow = PersistentWorkflowUtils.buildOpenWorkflow(user, XnatSubjectdata.SCHEMA_ELEMENT_NAME, subjectLabel, projectId, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, "Auto-created for project", "Created to support archiving small animal 'hotel' image session", "Creating new subject " + subjectLabel));
                assert workflow != null;
                workflow.setStepDescription("Creating");
                eventMeta = workflow.buildEvent();
            } catch (Exception e) {
                log.error("Unable to create 'Hotel' subject for project " + projectId, e);
                subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_FAILED, new Date(), "Unable to create 'Hotel' subject for project " + projectId);
                return;
            }

            try {
                SaveItemHelper.authorizedSave(subject, user, false, false, eventMeta);
                XDAT.triggerXftItemEvent(subject, CREATE);
                workflow.setStepDescription(PersistentWorkflowUtils.COMPLETE);
                WorkflowUtils.complete(workflow, eventMeta);
                log.info("Successfully create subject {} with ID {} in project {}", subjectLabel, subject.getId(), projectId);
                subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_COMPLETE, new Date(), "Created 'Hotel' subject for project " + projectId);
            } catch (Exception e) {
                subscriptionDeliveryEntityService.addStatus(deliveryId, ACTION_FAILED, new Date(), "Unable to create 'Hotel' subject for project " + projectId);
                workflow.setStepDescription(PersistentWorkflowUtils.FAILED);
                try {
                    WorkflowUtils.fail(workflow, eventMeta);
                } catch (Exception ex) {
                    log.error("Unable to fail workflow for project " + projectId, ex);
                }
                log.error("Unable to create 'Hotel' subject for project " + projectId, e);
            }
        } else {
            log.info("Ignoring event {} for project {} because it is not a project creation event", event.getDisplayName(), event.getProjectId());
        }
    }

    @Override
    public List<Action> getActions(String projectId, List<String> xnatTypes, UserI user) {
        if (xnatTypes == null || xnatTypes.isEmpty() || !xnatTypes.contains("xnat:projectData")) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Collections.singletonList(Action.builder().id(getName())
                                                                     .actionKey(getActionKey())
                                                                     .displayName(getDisplayName())
                                                                     .description(getDescription())
                                                                     .attributes(getAttributes(projectId, user))
                                                                     .provider(this)
                                                                     .build()));
        }
    }

    @Override
    public Boolean isActionAvailable(String actionKey, String projectId, UserI user) {
        return true;
    }

}
