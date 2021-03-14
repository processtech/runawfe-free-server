import Vue from 'vue';

Vue.use({ 
    install(Vue: any) {
        const swaggerClient = require('swagger-client');
        Vue.client = swaggerClient({
            url: 'http://localhost:8080/restapi/v3/api-docs',
        });
    }
});

