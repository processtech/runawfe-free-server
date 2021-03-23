import Vue from 'vue';

Vue.use({ 
    install(Vue: any) {
        Vue.prototype.$__ucfirst = function (str: string): string {
            str = str.toLowerCase();
            return str.charAt(0).toUpperCase() + str.slice(1);
        };
        Vue.prototype.$__copy = function (obj: any): any {
            let result: {[k: string]: any} = {};
            for (let prop in obj) {
                if (!obj.hasOwnProperty(prop) || obj[prop] instanceof Function) continue;
                if (obj[prop] instanceof String || 
                    obj[prop] instanceof Number || 
                    obj[prop] instanceof Boolean ||
                    obj[prop] === null) {
                        result[prop] = obj[prop];
                } else if (obj[prop] instanceof Array) {
                    result[prop] = [];
                    for (let item of obj[prop]) {
                        result[prop].push(Vue.prototype.$__copy(obj[prop]));
                    }
                } else if (obj[prop] instanceof Object) {
                    result[prop] = Vue.prototype.$__copy(obj[prop]);
                }
            }
            return result;
        }
    }
});

