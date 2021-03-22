import Vue from 'vue';

Vue.use({ 
    install(Vue: any) {
        Vue.prototype.$ucfirst = function (str: string): string {
            str = str.toLowerCase();
            return str.charAt(0).toUpperCase() + str.slice(1);
        }
    }
});

