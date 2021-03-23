// Registration of Global Components in ../components

import Vue from 'vue';
const requireComponent = require.context('../components', true, /\.vue$/);

for (const file of requireComponent.keys()) {
    const componentConfig = requireComponent(file);

    Vue.component(
        componentConfig.default.extendOptions.name,
        componentConfig.default || componentConfig,
    );
}
