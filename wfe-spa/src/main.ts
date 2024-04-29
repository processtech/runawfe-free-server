import '@mdi/font/css/materialdesignicons.css'
import 'vuetify/styles'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { apiClient } from './logic/api-client'
import { vuetify } from './plugins/vuetify'

import router from './plugins/router'
import App from './App.vue'
import VariableRow from './components/VariableRow.vue'
import ExpandingCell from './components/ExpandingCell.vue'
import BooleanVariableFormat from './components/formats/BooleanVariableFormat.vue'
import DateVariableFormat from './components/formats/DateVariableFormat.vue'
import DateTimeVariableFormat from './components/formats/DateTimeVariableFormat.vue'
import ExecutorVariableFormat from './components/formats/ExecutorVariableFormat.vue'
import FileVariableFormat from './components/formats/FileVariableFormat.vue'
import FormattedTextVariableFormat from './components/formats/FormattedTextVariableFormat.vue'
import NumberVariableFormat from './components/formats/NumberVariableFormat.vue'
import ListVariableFormat from './components/formats/ListVariableFormat.vue'
import ProcessVariableFormat from './components/formats/ProcessVariableFormat.vue'
import StringVariableFormat from './components/formats/StringVariableFormat.vue'
import TextVariableFormat from './components/formats/TextVariableFormat.vue'
import TimeVariableFormat from './components/formats/TimeVariableFormat.vue'
import UserTypeVariableFormat from './components/formats/UserTypeVariableFormat.vue'
import StringFilterFormat from './components/formats/StringFilterFormat.vue'
import LongFilterFormat from './components/formats/LongFilterFormat.vue'
import DateTimeFilterFormat from './components/formats/DateTimeFilterFormat.vue'
import OptionsFilterFormat from './components/formats/OptionsFilterFormat.vue'
import ProcessDefinitionCategoryFilterFormat from './components/formats/ProcessDefinitionCategoryFilterFormat.vue'

createApp(App).use(createPinia())
  .use({
    install(app: any) { // TODO 1) delete the $__ucfirst and $apiClient definitions 2) param type
      app.config.globalProperties.$__ucfirst = function (str: string): string {
          str = str.toLowerCase();
          return str.charAt(0).toUpperCase() + str.slice(1);
      };
      app.config.globalProperties.$apiClient = apiClient;
      /* register components chosen dynamically */
      const formatComponents = {
        VariableRow, // TODO check if this really should be registred
        ExpandingCell, // TODO the same
        BooleanVariableFormat,
        DateVariableFormat,
        DateTimeVariableFormat,
        ExecutorVariableFormat,
        FileVariableFormat,
        FormattedTextVariableFormat,
        NumberVariableFormat,
        ListVariableFormat,
        ProcessVariableFormat,
        StringVariableFormat,
        TextVariableFormat,
        TimeVariableFormat,
        UserTypeVariableFormat,
        StringFilterFormat,
        LongFilterFormat,
        DateTimeFilterFormat,
        OptionsFilterFormat,
        ProcessDefinitionCategoryFilterFormat,
      }
      Object.entries(formatComponents)
        .forEach(([name, component]) => app.component(name, component))
    }
  })
  .use(router)
  .use(vuetify)
  .mount('#app')
