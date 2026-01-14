/*
 * PIXI Projects
 *
 * Project related functions
 */

console.debug('pixi-projects.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = getObject(XNAT.plugin.pixi || {});
XNAT.plugin.pixi.projects = getObject(XNAT.plugin.pixi.projects || {});
XNAT.plugin.pixi.getOnlyOwners = getObject(XNAT.plugin.pixi.getOnlyOwners || {});

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

    XNAT.plugin.pixi.projects.getAll = async function() {
        console.debug(`pixi-projects.js: XNAT.plugin.pixi.projects.getAll`);

        let projectUrl = XNAT.url.restUrl('/data/projects');
        projectUrl = XNAT.url.addQueryString(projectUrl, ['prearc_code=true',
                                                          'recent=true',
                                                          'owner=true',
                                                          'member=true',
                                                          'collaborator=true']);

        const response = await fetch(projectUrl, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        return response.json()
    }

    XNAT.plugin.pixi.projects.getOnlyOwners = async function() {
        console.debug(`pixi-projects.js: XNAT.plugin.pixi.projects.getOnlyOwners`);

        let projectUrl = XNAT.url.restUrl('/data/projects');
        projectUrl = XNAT.url.addQueryString(projectUrl, ['prearc_code=true',
                                                          'recent=true',
                                                          'owner=true',
                                                          'member=false',
                                                          'collaborator=false']);

        const response = await fetch(projectUrl, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        return response.json()
    }

    XNAT.plugin.pixi.projects.populateSelectBox = async function(projectSelectorId) {
        let projectSelectorEl = document.getElementById(projectSelectorId);

        return XNAT.plugin.pixi.projects.getAll()
            .then(resultSet => resultSet['ResultSet']['Result'])
            .then(projects => {
                // Clear select box
                projectSelectorEl.options.length = 0;

                // Placeholder
                projectSelectorEl.options[0] = new Option("");
                projectSelectorEl.options[0].disabled = true;
                projectSelectorEl.options[0].selected = true;

                projects.forEach(project => {
                    let projectOption = new Option(project['id'], project['id']);
                    projectSelectorEl.options[projectSelectorEl.length] = projectOption

                    if (XNAT.data.context.projectID && project['id'] === XNAT.data.context.projectID) {
                        // If project is set in the XNAT context then select it.
                        projectOption.selected = true;
                    }
                })
            });
    }

}));