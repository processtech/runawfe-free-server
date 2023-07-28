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
                                    v-disabled-icon-focus
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
                                    v-disabled-icon-focus
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
            hasError: false,
            forwardUrl: '',
        }
    },
    directives: {
        disabledIconFocus: {
            bind(el) {
                el.querySelectorAll('.v-input__icon button').forEach(x => x.setAttribute('tabindex', -1));
            },
        },
    },
    methods: {
        login() {
            this.$store.dispatch('user/login', {
                username: this.username,
                password: this.password
            }).then(isAuthenticated => {
                const query = window.location.toString().split("?")[1];
                if (query) {
                    if (query.includes('forwardUrl=')) {
                        this.forwardUrl = decodeURIComponent(query.replace('forwardUrl=', ''));
                    }
                }
                this.navigateAfterLogin();
            }, error => {
                this.error = error;
                this.hasError = true;
            }).catch((error: any) => {
                console.log(error);
            });
        },
        navigateAfterLogin() {
            if (this.forwardUrl) {
                this.$router.push({ path: this.forwardUrl }).catch((error: any) => {
                    console.log(error);
                });
            } else {
                this.$router.push({ name: 'Мои задачи' }).catch((error: any) => {
                    console.log(error);
                });
            }
        },
    }
})
</script>