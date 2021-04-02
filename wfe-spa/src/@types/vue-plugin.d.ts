import Vue from 'vue';

// Vue имеет тип конструктора в types/vue.d.ts
declare module 'vue/types/vue' {
  interface Vue {
    $apiClient: any;
    $__ucfirst: any;
    $__copy: any;
  }
}