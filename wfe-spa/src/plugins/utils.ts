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
                    obj[prop] === undefined ||
                    obj[prop] === null) {
                        result[prop] = obj[prop];
                } else if (obj[prop] instanceof Array) {
                    result[prop] = [];
                    for (let item of obj[prop]) {
                        if (item instanceof Object) {
                            result[prop].push(Vue.prototype.$__copy(item));
                        } else {
                            result[prop].push(item);
                        }
                    }
                } else if (obj[prop] instanceof Object) {
                    result[prop] = Vue.prototype.$__copy(obj[prop]);
                }
            }
            return result;
        };
        Vue.prototype.$apiClient = function (): any {
            return new Promise((resolve, reject) => {
                const client = this.$store.state.app.swagger;
                const token = this.$store.state.user.token;
                reject = (reason: any) => this.$router.push({ name: 'Login' });
                if (!!!token) {
                    reject(null);
                } else if (!client) {
                    this.$store.dispatch('user/makeSwaggerClient', { token, resolve, reject });
                } else {
                    this.$store.dispatch('user/validateToken', { token, client, resolve, reject });
                }
            });
        };
    }
});

/* formats as 'dd.mm.yyyy hh:mm' */
export function formatDate(date: Date): string {
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${pad(date.getDate())}.${pad(date.getMonth() + 1)}.${date.getFullYear()} `
    + `${pad(date.getHours())}:${pad(date.getMinutes())}`
}
