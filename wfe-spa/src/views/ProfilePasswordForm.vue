<template>
  <v-card flat width="500" class="mx-auto my-10">
      <v-alert dense text type="success" class="mx-auto mb-5" v-show="success">
        Пароль был успешно изменен
      </v-alert>
    <v-form>
      <v-text-field
        ref="password"
        v-model="password"
        :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :type="showPassword ? 'text' : 'password'"
        :rules="[rules.required]"
        label="Введите новый пароль"
        @click:append="showPassword = !showPassword"
      />
      <v-text-field
        ref="confirmedPassword"
        v-model="confirmedPassword"
        :append-icon="showConfirmedPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :type="showConfirmedPassword ? 'text' : 'password'"
        :rules="[rules.required, rules.confirm]"
        label="Подтвердите пароль"
        @click:append="showConfirmedPassword = !showConfirmedPassword"
      />
      <v-btn
        color="accent"
        class="mt-5"
        :disabled="!valid"
        @click="changePassword"
      >
        Применить
      </v-btn>
    </v-form>
  </v-card>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'ProfilePasswordForm',
  data() {
    return {
      valid: false,
      success: false,
      showPassword: false,
      showConfirmedPassword: false,
      password: '',
      confirmedPassword: '',
      rules: {
        required: value => !!value || 'Обязательное поле',
        confirm: value => value === this.password || 'Пароли не совпадают'
      },
    }
  },

  methods: {
    changePassword() {
      const request = {
        parameters: { password: this.password }
      };
      this.$apiClient().then(client => {
        client['profile-controller'].changePasswordUsingPOST(null, request)
          .then(data => {
            if (data.ok) {
              this.success = true;
              setTimeout(() => this.success = false, 3000);
            }
        });
      });
    },
    validate() {
      this.valid = this.$refs.password.validate()
        && this.$refs.confirmedPassword.validate();
    }
  },

  watch: {
    password: function() {
      this.validate();
    },
    confirmedPassword: function() {
      this.validate();
    }
  }
});
</script>
