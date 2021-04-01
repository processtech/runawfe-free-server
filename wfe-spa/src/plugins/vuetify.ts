import Vue from 'vue';
import Vuetify from 'vuetify';

Vue.use(Vuetify);

const theme = {
  primary: '#4CAF50',
  secondary: '#9C27b0',
  accent: '#4CAF50',
  info: '#00CAE3',
  success: '#4CAF50',
  warning: '#FB8C00',
  error: '#FF5252',
}

export default new Vuetify({
  breakpoint: {
    mobileBreakpoint: 960
  },
  icons: {
    iconfont: 'mdi'
  },
  theme: {
    themes: {
      dark: theme,
      light: theme,
    },
  },
});
