package org.nrg.xnatx.plugins.pixi.hotelsplitter.initialize;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xnat.eventservice.exceptions.SubscriptionAccessException;
import org.nrg.xnat.eventservice.exceptions.SubscriptionValidationException;
import org.nrg.xnat.eventservice.model.EventFilterCreator;
import org.nrg.xnat.eventservice.model.EventServicePrefs;
import org.nrg.xnat.eventservice.model.Subscription;
import org.nrg.xnat.eventservice.model.SubscriptionCreator;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.initialization.tasks.AbstractInitializingTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for creating the subscription for project created events that will be used to automatically
 * create the hotel subject.
 */
@Component
@Slf4j
public class CreateHotelSubjectEventSubscription extends AbstractInitializingTask {

    private final EventService eventService;

    @Autowired
    public CreateHotelSubjectEventSubscription(final EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public String getTaskName() {
        return "CreateHotelSubjectEventSubscription";
    }

    @Override
    protected void callImpl() {
        EventServicePrefs prefs = eventService.getPrefsPojo();
        boolean isEventServiceEnabled = prefs.enabled() != null && prefs.enabled();

        if (!isEventServiceEnabled) {
            log.debug("Event service is not enabled. Enabling it.");
            eventService.updatePrefs(EventServicePrefs.create(true));
        }

        boolean subscriptionExists;
        String actionKey = "org.nrg.xnatx.plugins.pixi.hotelsplitter.eventservice.actions.PixiHotelSubjectAction" +
                ":org.nrg.xnatx.plugins.pixi.hotelsplitter.eventservice.actions.PixiHotelSubjectAction";

        try {
            subscriptionExists = eventService.getSubscriptions()
                    .stream()
                    .anyMatch(subscription -> subscription.actionKey().equals(actionKey));
        } catch (SubscriptionAccessException e) {
            log.error("Error getting subscriptions", e);
            return;
        }

        if (subscriptionExists) {
            log.debug("Auto Create Hotel Subject event service subscription already exists. Skipping initialization.");
            return;
        }

        EventFilterCreator eventFilter = EventFilterCreator.builder()
                .eventType("org.nrg.xnat.eventservice.events.ProjectEvent")
                .schedule("")
                .scheduleDescription("")
                .jsonPathFilter("")
                .status("CREATED")
                .build();

        SubscriptionCreator subscription = SubscriptionCreator.builder()
                .name("Auto Create Hotel Subject")
                .active(true)
                .actionKey(actionKey)
                .eventFilter(eventFilter)
                .actAsEventUser(true)
                .build();

        final Subscription toCreate = Subscription.create(subscription, Users.getAdminUser().getLogin());
        try {
            Subscription created = eventService.createSubscription(toCreate, false);

            if (created == null) {
                log.error("Error creating hotel subject event service subscription");
            } else {
                log.info("Created 'Auto Create Hotel Subject' event service subscription");
            }
        } catch (SubscriptionValidationException | SubscriptionAccessException e) {
            log.error("Error creating subscription", e);
        }
    }
}
