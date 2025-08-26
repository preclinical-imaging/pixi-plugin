(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function() {
    XNAT.plugin.pixi = getObject(XNAT.plugin.pixi || {});

    // Form Elements
    let formEl             = document.getElementById("upload-form");
    let projectEl          = document.getElementById("project");
    let projectFilerInput  = document.getElementById("project-filter");
    let dataOverlapElement = document.getElementById("data_overlap_handling");
    let mappingEl          = document.getElementById("mapping");
    let fileEl             = document.getElementById("file");
    let uploadButton       = document.getElementById("upload-button");

    // Form rendering
    let renderProjectSelectBox = function(selectBox, projects) {
        // Clear select box
        selectBox.options.length = 0;

        projects.forEach(project => {
            let keys = Object.keys(project)
            const permissions_key = keys.filter((key) => key.startsWith("user_role"));
            if (project[permissions_key] != "Owners") {
                return;
            }
            selectBox.options[selectBox.length] = new Option(project['id'], project['id'])
        })
        if (selectBox.options.length === 0) {
            XNAT.ui.dialog.open({
                title: 'No Projects Available For Upload',
                content: `<div class="info">Biodistribution upload is currently limited to project owners. As you are not an owner of any project you are not able to upload biodistribution data.</div>`,
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true,
                    }
                ]
            })
        }
    }

    // Load projects and mappings then render select boxes
    XNAT.plugin.pixi.projects.getOnlyOwners()
            .then(resultSet => resultSet['ResultSet']['Result'])
            .then(projects => renderProjectSelectBox(projectEl, projects));

    // Event listeners
    let filterProject = (event) => {
        let filter = event.target.value.toLowerCase();
        let projects = Array.from(projectEl.options);

        projects.forEach(project => {
            project.hidden = !project.value.toLowerCase().includes(filter);
        });
    }

    let alertUserOfOverwriteDecision = (event) => {
        if (event.target.value === "upload_overwrite") {
            XNAT.ui.dialog.open({
                title: 'Data Overlap Method Changed',
                content: `<div class="info">Setting the method of handling data overlap may cause data in the system to be overwritten. Only upload data with this set if you know there is no chance of data loss.</div>`,
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true,
                    }
                ]
            })
        }
    }

    function createSubjectList(subjects) {
            let subjectList = '';
            for (const subjectId of subjects) {
                let subjectHtmlElement = `${subjectId} <br>`;
                subjectList = subjectList.concat(subjectHtmlElement);
            }
            return subjectList;
        }

    function getProjectLink(id) {
        return XNAT.url.rootUrl('data/projects/' + id);
    }

    function createExperimentLink(experimentId) {
        return XNAT.url.rootUrl('app/action/DisplayItemAction/search_value/' + experimentId + '/search_element/pixi:biodistributionData/search_field/pixi:biodistributionData.ID')
    }

    function createExperimentDetailsList(allExperiments) {
        let fullDetailsList = ''
        for (const experimentId in allExperiments) {
            let experimentLabel = allExperiments[experimentId];
            let experimentLink = createExperimentLink(experimentId);
            let experimentHtmlElement = `<a href=${experimentLink}>${experimentLabel}</a><br>`;
            fullDetailsList = fullDetailsList.concat(experimentHtmlElement);
        }
        fullDetailsList = fullDetailsList.substring(0, fullDetailsList.length - 4);
        return fullDetailsList;
    }

    function createSuccessPrintoutMessage(projectId, data) {
        let project_link = getProjectLink(projectId);
        let experimentsList = createExperimentDetailsList(data);

        if (!jQuery.isEmptyObject(data)) {
            return `<div class="success">Upload and extraction successful. Data has been successfully added to <a href=${project_link}>${projectId}.</a>
                <details>
                      <summary>The following Biodistribution experiments have been created: </summary>${experimentsList}</details></div>`
        } else {
            return `<div class="success">Upload and extraction successful. However, all input data overlaps with existing data and you have selected the skip matching data option. As such, no new data was uploaded to the system.</div>`
        }
    }

    function peformBiodistributionUpload(projectId, uploadId, encodedFileName, dataOverlapHandling) {
        let biodCreateApiUrl = XNAT.url.csrfUrl('/xapi/pixi/biodistribution/create');
        let biodCreateApiUrlWithParams = XNAT.url.addQueryString(biodCreateApiUrl, [
            `project=${projectId}`,
            `cachePath=${uploadId}/${encodedFileName}`,
            `dataOverlapHandling=${dataOverlapHandling}`
        ]);

        xmodal.loading.open({ title: 'Uploading Biodistribution Data'});
        XNAT.xhr.post({
            url: biodCreateApiUrlWithParams,
            success: function (data) {
                XNAT.ui.dialog.close("biod_upload");
                xmodal.loading.close();
                XNAT.ui.dialog.open({
                    title: 'Upload Successful',
                    content: createSuccessPrintoutMessage(projectId, data),
                    buttons: [
                        {
                            label: 'OK',
                            isDefault: true,
                            close: true,
                        }
                    ]
                })
            },
            error: function(e) {
                console.error(`Failed to extract ${file.name}`)
                XNAT.ui.dialog.close("biod_upload");
                xmodal.loading.close();

                let errorTextWithBreaks = e.responseText.replaceAll("\n", "<br>");

                XNAT.ui.dialog.open({
                    title: 'Extraction Failed',
                    content: `<div class="error">Failed to extract biodistribution data from ${file.name}. <br>${errorTextWithBreaks}</div>`,
                    buttons: [
                        {
                            label: 'OK',
                            isDefault: true,
                            close: true,
                        }
                    ]
                })
            }
        });
    }

    projectFilerInput.addEventListener('keyup', filterProject);
    dataOverlapElement.addEventListener('change', alertUserOfOverwriteDecision);

    uploadButton.onclick =  async function submitForm() {
        if (!(XNAT.validate(projectEl).required().check() || XNAT.validate(fileEl).is('fileType', 'csv').check())) {
            console.debug('Uploader form is invalid.');
            return;
        }

        let projectId = projectEl.value;
        let dataOverlapHandling = dataOverlapElement.value;
        let uploadId = new Date().toISOString()
                .replaceAll('-', '_')
                .replaceAll(':', '_')
                .replaceAll('.', '_');

        let file = fileEl.files[0];
        let encodedFileName = encodeURIComponent(file.name);
        encodedFileName = encodedFileName.replaceAll('%20', '_'); // Replace spaces with underscores
        let cachePath = `/user/cache/resources/${uploadId}/files/${encodedFileName}`;
        let encodedCachePath = encodeURIComponent(cachePath);
        let userResourceCacheUrl = XNAT.url.csrfUrl(`/data/${cachePath}`);

        let formDataFileOnly = new FormData()
        formDataFileOnly.append('file', file)

        // Upload the zip file to the users cache
        XNAT.ui.dialog.static.wait(`Uploading ${file.name}`,{id: "biod_upload"});

        let response = await fetch(userResourceCacheUrl, {
            method: 'PUT',
            body : formDataFileOnly
        })

        if (response.ok) {
            console.debug('Upload to cache successful');
        } else {
            console.error(`Failed to upload ${file.name}`)
            XNAT.ui.dialog.close("biod_upload");
            XNAT.ui.dialog.open({
                title: 'Upload Failed',
                content: `<div class="error">Failed to upload ${file.name}.</div>`,
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true,
                    }
                ]
            })
            return;
        }

        let biodPreprocessingUrl = XNAT.url.csrfUrl('/xapi/pixi/biodistribution/preprocessing');
        let biodPreprocessingApiUrlWithParams = XNAT.url.addQueryString(biodPreprocessingUrl, [
            `project=${projectId}`,
            `cachePath=${uploadId}/${encodedFileName}`
        ]);
        XNAT.xhr.post({
            url: biodPreprocessingApiUrlWithParams,
            success: function (data) {
                XNAT.ui.dialog.close("biod_upload");
                if (data != undefined && data.length > 0) {
                    let subjectList = createSubjectList(data);
                    XNAT.ui.dialog.open({
                        title: 'Subjects To Be Created',
                        content: `<div>The following subjects are not present and will be created.
                                  <details><summary></summary>${subjectList}</details></div>`,
                        buttons: [
                            {
                                label: 'Cancel',
                                isDefault: false,
                                close: true,
                                action: function() {
                                    return;
                                }
                            },
                            {
                                label: 'Create',
                                isDefault: true,
                                close: true,
                                action: function() {
                                    peformBiodistributionUpload(projectId, uploadId, encodedFileName, dataOverlapHandling);
                                }
                            }
                        ]
                    })
                } else {
                    peformBiodistributionUpload(projectId, uploadId, encodedFileName, dataOverlapHandling)
                }
            },
            error: function(e) {
                console.error(`Failed to extract ${file.name}`)
                XNAT.ui.dialog.close("biod_upload");
                let errorTextWithBreaks = e.responseText.replaceAll("\n", "<br>");
                XNAT.ui.dialog.open({
                    title: 'Preprocessing Failed',
                    content: `<div class="error">Failed to extract subject data from ${file.name}. <br>${errorTextWithBreaks}</div>`,
                    buttons: [
                        {
                            label: 'OK',
                            isDefault: true,
                            close: true,
                        }
                    ]
                })
                return;
            }
        });
    }
}));