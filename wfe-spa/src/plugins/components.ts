// Registration of Global Components in ../components

import Vue from 'vue';
import upperFirst from 'lodash/upperfirst';
import camelCase from 'lodash/camelCase';
const requireComponent = require.context('../components', true, /\.vue$/);
  
requireComponent.keys().forEach((file: string) => {
    let fileName = file.split('/').pop();
    if (fileName !== undefined) {
        const componentName = upperFirst(camelCase(fileName.replace(/\.\w+$/, '')));
        const componentConfig = requireComponent(file);
        Vue.component(
            componentName, 
            componentConfig.default || componentConfig
        );
    }
});
