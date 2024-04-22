<template>
  <v-app>
    <v-main>
      <v-container fluid class="bg-secondary d-flex align-center" style="height: 100%">
        <v-row justify="center">
          <v-col md="3">
            <v-alert v-model="hasError" dense outlined type="error" dismissible>
              {{ error }}
            </v-alert>
            <v-card class="pa-3" color="bg-primary-background">
              <v-form class="login" @submit.prevent="login">
                <v-img
                  width="160"
                  :src="`${publicPath}/${themeContrast}/logo-text.png`"
                  class="mb-3 mx-auto"
                />
                <v-text-field placeholder="Введите логин" v-model="username" />
                <v-text-field
                  placeholder="Введите пароль"
                  :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  :type="showPassword ? 'text' : 'password'"
                  v-model="password"
                  outlined
                  dense
                  @click:appendInner="showPassword = !showPassword"
                />
                <v-btn type="submit" color="primary" block>Войти</v-btn>
              </v-form>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script lang="ts">
import { mapActions, mapState } from 'pinia';
import { defineComponent } from 'vue';
import { useThemeStore } from '../stores/theme-store';
import { useAuthStore } from '../stores/auth-store';
import { useSystemStore } from '../stores/system-store';
import {executorService} from '@/services/executor-service';
import {systemConfiguration} from '@/logic/system-configuration';

export default defineComponent({
    name: 'Login',

    data() {
        return {
            username: '',
            password: '',
            showPassword: false,
            error: '',
            hasError: false,
            forwardUrl: '',
            publicPath: systemConfiguration.publicPath()
        }
    },

    computed: {
      ...mapState(useThemeStore, ['themeContrast']),
    },

    methods: {
        ...mapActions(useAuthStore, { doLogin: 'login', saveUser: 'saveUser' }),

        login() {
            this.doLogin({
                username: this.username,
                password: this.password
            }).then(isAuthenticated => {
                const query = window.location.toString().split("?")[1];
                if (query) {
                    if (query.includes('forwardUrl=')) {
                        this.forwardUrl = decodeURIComponent(query.replace('forwardUrl=', ''));
                    }
                }
                executorService.getUserByName(this.username)
                  .then(user => this.saveUser(user))
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
