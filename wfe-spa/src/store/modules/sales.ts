// Pathify
import { make } from 'vuex-pathify'

const state = {
  sales: [
    {
      country: 'USA',
      flag: 'https://demos.creative-tim.com/vue-material-dashboard-pro/img/flags/US.png',
      salesInM: 2920,
    },
    {
      country: 'Germany',
      flag: 'https://demos.creative-tim.com/vue-material-dashboard-pro/img/flags/DE.png',
      salesInM: 1300,
    },
    {
      country: 'Australia',
      flag: 'https://demos.creative-tim.com/vue-material-dashboard-pro/img/flags/AU.png',
      salesInM: 760,
    },
    {
      country: 'United Kingdom',
      flag: 'https://demos.creative-tim.com/vue-material-dashboard-pro/img/flags/GB.png',
      salesInM: 690,
    },
    {
      country: 'Romania',
      flag: 'https://demos.creative-tim.com/vue-material-dashboard-pro/img/flags/RO.png',
      salesInM: 600,
    },
    {
      country: 'Brasil',
      flag: 'https://demos.creative-tim.com/vue-material-dashboard-pro/img/flags/BR.png',
      salesInM: 550,
    },
  ],
}

const mutations = make.mutations(state)

const actions = {}

const getters = {}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
}
