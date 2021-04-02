<template>
  <v-card flat width="500" class="mx-auto my-10">
    <v-form>
      <v-text-field
        v-model="password"
        :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :rules="[rules.required]"
        :type="showPassword ? 'text' : 'password'"
        name="password"
        label="Введите старый пароль"
        counter
        @click:append="showPassword = !showPassword"
      />
      <v-text-field
        v-model="newPassword"
        :append-icon="showNewPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :rules="[rules.required]"
        :type="showNewPassword ? 'text' : 'password'"
        name="newPassword"
        label="Введите новый пароль"
        counter
        @click:append="showNewPassword = !showNewPassword"
      />
      <v-text-field
        v-model="confirmedPassword"
        :append-icon="showConfirmedPassword ? 'mdi-eye' : 'mdi-eye-off'"
        :rules="[rules.required]"
        :type="showConfirmedPassword ? 'text' : 'password'"
        name="confirmedPassword"
        label="Подтвердите пароль"
        counter
        @click:append="showConfirmedPassword = !showConfirmedPassword"
      />
      <v-btn color="primary" class="mt-5">Применить</v-btn>
    </v-form>
  </v-card>
</template>

<script lang="ts">
import Vue from 'vue';

export default Vue.extend({
  name: 'ProfilePasswordForm',
  data() {
    return {
      showPassword: false,
      showNewPassword: false,
      showConfirmedPassword: false,
      password: '',
      newPassword: '',
      confirmedPassword: '',
      rules: {
        required: (value: any): string|boolean => {
          return !!value || 'Обязательное поле';
        },
      },
    }
  }
})
</script>
