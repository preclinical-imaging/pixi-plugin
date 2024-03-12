/*
 * PIXI Projects
 *
 * Project related functions
 */

console.debug('pixi-projects.js');

var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = getObject(XNAT.plugin.pixi || {});
XNAT.plugin.pixi.subjects = getObject(XNAT.plugin.pixi.subjects || {});

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

    XNAT.plugin.pixi.subjects.get = async function(projectId, subjectLabel) {
        console.debug(`pixi-subjects.js: XNAT.plugin.pixi.subjects.get`);

        let subjectUrl = XNAT.url.restUrl(`/data/projects/${projectId}/subjects/${subjectLabel}`);
        subjectUrl = XNAT.url.addQueryString(subjectUrl, ['format=json']);

        const response = await fetch(subjectUrl, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error("Subject does not exist.");
        }
        
        let json = await response.json();
        
        let subject = {
            'id': json['items'][0]['data_fields']['ID'],
            'label': json['items'][0]['data_fields']['label'],
            'project': json['items'][0]['data_fields']['project'],
            'group': json['items'][0]['data_fields']['group'],
        }
        
        let demographicsJson = json['items'][0]['children'].filter(child => child['field'] === 'demographics')
        
        if (demographicsJson.length === 1) {
            // Has demographics
            let demographicsData = demographicsJson[0]['items'][0]['data_fields'];
            subject = {
                ...subject,
                'species': demographicsData['species'] ? XNAT.plugin.pixi.unescapeHtml(demographicsData['species']) : '',
                'sex': demographicsData['sex'] ? XNAT.plugin.pixi.unescapeHtml(demographicsData['sex']) : '',
                'dateOfBirth': demographicsData['dateOfBirth'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['dateOfBirth']) : '',
                'litter': demographicsData['litter'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['litter']) : '',
                'strain': demographicsData['strain'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['strain']) : '',
                'source': demographicsData['source'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['source']) : '',
                'stockNumber': demographicsData['stockNumber'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['stockNumber']) : '',
                'strainImmuneSystemHumanizationType': demographicsData['strainImmuneSystemHumanizationType'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['strainImmuneSystemHumanizationType']) : '',
                'geneticModifications': demographicsData['geneticModifications'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['geneticModifications']) : '',
                'geneticModificationsSecondary': demographicsData['geneticModificationsSecondary'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['geneticModificationsSecondary']) : '',
                'dateOfDeath': demographicsData['dateOfDeath'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['dateOfDeath']) : '',
                'endpoint': demographicsData['endpoint'] ? XNAT.plugin.pixi.unescapeHtml(demographicsJson[0]['items'][0]['data_fields']['endpoint']) : ''
            }
        }
        
        return subject;
    }

    XNAT.plugin.pixi.subjects.getAll = async function(projectId) {
        console.debug(`pixi-subjects.js: XNAT.plugin.pixi.subjects.getAll`);

        let subjectUrl = XNAT.url.restUrl(`/data/projects/${projectId}/subjects`);
        subjectUrl = XNAT.url.addQueryString(subjectUrl, ['format=json']);

        const response = await fetch(subjectUrl, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        })

        if (!response.ok) {
            throw new Error("Failed to fetch subjects.");
        }

        return response.json();
    }

    XNAT.plugin.pixi.subjects.createOrUpdate = async function(projectId, subjectLabel, group = null, species = null,
                                                              sex = null, dob = null, litter = null, strain = null,
                                                              source = null, stockNumber = null, humanizationType = null,
                                                              geneticModifications = null, geneticModificationsNonStd = null,
                                                              dateOfDeath = null, endpoint = null) {
        console.debug(`pixi-subjects.js: XNAT.plugin.pixi.subjects.create`);

        let subjectUrl = XNAT.url.csrfUrl(`/data/projects/${projectId}/subjects/${subjectLabel}`);

        let queryString = []
        let addQueryString = (xmlPath, data) => {
            if (data !== null && data !== '') {
                let encodedXmlPath = XNAT.url.encodeURIComponent(xmlPath);
                let encodedData = XNAT.url.encodeURIComponent(data);
                queryString.push(`${encodedXmlPath}=${encodedData}`)
            }
        }

        addQueryString('xnat:subjectData/group', group);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/species', species);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/sex', sex);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/dateOfBirth', dob);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/litter', litter);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/strain', strain);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/source', source);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/stockNumber', stockNumber);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/strainImmuneSystemHumanizationType', humanizationType);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/geneticModifications', geneticModifications);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/geneticModificationsSecondary', geneticModificationsNonStd);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/dateOfDeath', dateOfDeath);
        addQueryString('xnat:subjectData/demographics[@xsi:type=pixi:animalDemographicData]/endpoint', endpoint);

        subjectUrl = XNAT.url.addQueryString(subjectUrl, queryString);

        const response = await fetch(subjectUrl, {method: 'PUT'});

        if (!response.ok) {
            throw new Error(`Unable to create/update subject ${subjectLabel} ${response.statusText}`);
        }

        return response.url;
    }

}));