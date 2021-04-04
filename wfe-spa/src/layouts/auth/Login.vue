<template>
    <v-app>
        <v-main>
            <v-container fill-height fluid style="background: #EEEEEE">
                <v-row justify="center" align="center">
                    <v-col md="3">
                        <v-alert 
                            v-model="hasError" 
                            dense 
                            outlined 
                            type="error"
                            dismissible
                        >
                            {{ error }}
                        </v-alert>
                        <v-card class="pa-2">
                            <v-form class="login" @submit.prevent="login">
                                <v-img 
                                    max-height="50px" 
                                    max-width="227px" 
                                    :src="require('../../assets/big_logo.gif')" 
                                    style="margin-bottom: 1em"
                                ></v-img>
                                <v-text-field 
                                    color="#616161"
                                    placeholder="Введите логин" 
                                    v-model="username" 
                                    outlined 
                                    dense 
                                    clearable
                                ></v-text-field>
                                <v-text-field 
                                    color="#616161"
                                    placeholder="Введите пароль" 
                                    :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                                    :type="showPassword ? 'text' : 'password'"
                                    v-model="password" 
                                    outlined 
                                    dense 
                                    clearable
                                    @click:append="showPassword = !showPassword"
                                ></v-text-field>
                                <v-btn type="submit" color="primary" block>
                                    Войти
                                </v-btn>
                            </v-form>
                        </v-card>
                    </v-col>
                </v-row>
            </v-container>
        </v-main>
    </v-app>
</template>

<script lang="ts">
import Vue from 'vue';
import { get, sync } from 'vuex-pathify';

export default Vue.extend({
    name: 'Login',
    
    data() {
        return {
            username: '',
            password: '',
            showPassword: false,
            error: '',
            hasError: false
        }
    },

    methods: {
        login() {
            this.$store.dispatch('user/login', {
                username: this.username,
                password: this.password
            }).then(isAuthenticated => {
                this.$router.push({ name: 'Рабочий стол' }).catch((error: any) => {
                    console.log(error);
                });
            }, error => {
                this.error = error;
                this.hasError = true;
            }).catch((error: any) => {
                console.log(error);
            });
        }
    }
})
</script>