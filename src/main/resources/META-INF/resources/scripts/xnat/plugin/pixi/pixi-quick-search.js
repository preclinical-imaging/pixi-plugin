var XNAT = getObject(XNAT || {});
XNAT.plugin = getObject(XNAT.plugin || {});
XNAT.plugin.pixi = pixi = getObject(XNAT.plugin.pixi || {});

const quickSearchSubjectTab$ = document.getElementById("quick-search-subject");
const quickSearchMrTab$      = document.getElementById("quick-search-mrSessionData");
const quickSearchPetTab$     = document.getElementById("quick-search-petSessionData");
const quickSearchCtTab$      = document.getElementById("quick-search-ctSessionData");
const quickSearchBliTab$   = document.getElementById("quick-search-bliSessionData");

const config = { attributes: true, false: true, subtree: false };

const subjectSearchTabLoadedCallback = function(mutationsList, observer) {
    for(const mutation of mutationsList) {
        if (mutation.type === 'attributes' && mutation.attributeName === 'data-loaded') {
            console.debug('Subject search tab loaded.');
            XNAT.plugin.pixi.speciesPreferenceManager.setSelectOptions("quick_search_pixi_species_select")
            XNAT.plugin.pixi.demographicDataPreferenceManager.uiShowHumanSearchFields("xnat-subject-data-fields")
        }
    }
};

const mrSearchTabLoadedCallback = function(mutationsList, observer) {
    for(const mutation of mutationsList) {
        if (mutation.type === 'attributes' && mutation.attributeName === 'data-loaded') {
            console.debug('MR search tab loaded.');
            XNAT.plugin.pixi.speciesPreferenceManager.setSelectOptions("mr_quick_search_pixi_species_select")
            XNAT.plugin.pixi.demographicDataPreferenceManager.uiShowHumanSearchFields("mr-xnat-subject-data-fields")
        }
    }
};

const petSearchTabLoadedCallback = function(mutationsList, observer) {
    for(const mutation of mutationsList) {
        if (mutation.type === 'attributes' && mutation.attributeName === 'data-loaded') {
            console.debug('PET search tab loaded.');
            XNAT.plugin.pixi.speciesPreferenceManager.setSelectOptions("pet_quick_search_pixi_species_select")
            XNAT.plugin.pixi.demographicDataPreferenceManager.uiShowHumanSearchFields("pet-xnat-subject-data-fields")
        }
    }
};

const ctSearchTabLoadedCallback = function(mutationsList, observer) {
    for(const mutation of mutationsList) {
        if (mutation.type === 'attributes' && mutation.attributeName === 'data-loaded') {
            console.debug('CT search tab loaded.');
            XNAT.plugin.pixi.speciesPreferenceManager.setSelectOptions("ct_quick_search_pixi_species_select")
            XNAT.plugin.pixi.demographicDataPreferenceManager.uiShowHumanSearchFields("ct-xnat-subject-data-fields")
        }
    }
};

const bliSearchTabLoadedCallback = function(mutationsList, observer) {
    for(const mutation of mutationsList) {
        if (mutation.type === 'attributes' && mutation.attributeName === 'data-loaded') {
            console.debug('BLI search tab loaded.');
            XNAT.plugin.pixi.speciesPreferenceManager.setSelectOptions("bli_quick_search_pixi_species_select")
        }
    }
};

const subjectTabObserver = new MutationObserver(subjectSearchTabLoadedCallback);
subjectTabObserver.observe(quickSearchSubjectTab$, config);

const mrTabObserver = new MutationObserver(mrSearchTabLoadedCallback);
mrTabObserver.observe(quickSearchMrTab$, config);

const petTabObserver = new MutationObserver(petSearchTabLoadedCallback);
petTabObserver.observe(quickSearchPetTab$, config);

const ctTabObserver = new MutationObserver(ctSearchTabLoadedCallback);
ctTabObserver.observe(quickSearchCtTab$, config);

const bliTabObserver = new MutationObserver(bliSearchTabLoadedCallback);
bliTabObserver.observe(quickSearchBliTab$, config);