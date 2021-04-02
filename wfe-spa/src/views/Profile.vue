<template>
  <v-container
      id="profile-view"
      fluid
      tag="section"
  >
    <v-card>
      
      <v-toolbar flat color="transparent" class="my-4 pt-3">
        <v-container class="d-flex">
          <v-icon x-large>mdi-alien</v-icon>
          <v-container v-if="user" class="mx-3">
            <v-toolbar-title>{{ user.name }}</v-toolbar-title>
            <p class="text-caption">{{ user.description }}</p>
          </v-container>
        </v-container>
      </v-toolbar>

      <v-tabs vertical>
        <v-tab class="justify-start">
          <v-icon left>mdi-account-details-outline</v-icon>Информация пользователя
        </v-tab>
        <v-tab class="justify-start">
          <v-icon left>mdi-lock</v-icon>Изменение пароля
        </v-tab>
        <v-tab class="justify-start">
          <v-icon left>mdi-camera-outline</v-icon>Изменение аватара
        </v-tab>
        <v-tab class="justify-start">
          <v-icon left>mdi-account-switch-outline</v-icon>Возможные заместители
        </v-tab>
        <v-tab class="justify-start">
          <v-icon left>mdi-account-star</v-icon>Можете замещать
        </v-tab>
        <v-tab class="justify-start">
          <v-icon left>mdi-account-group</v-icon>Ваши группы
        </v-tab>
        <v-tab class="justify-start">
          <v-icon left>mdi-state-machine</v-icon>Список отношений
        </v-tab>

        <v-tab-item>
          <profile-user-card v-if="user" :user="user"/>
        </v-tab-item>
        <v-tab-item>
          <profile-password-form />
        </v-tab-item>
        <v-tab-item>
          <v-card flat width="500" class="mx-auto my-10">
            <v-file-input
              :rules="[rules.fileSize]"
              accept="image/png, image/jpeg, image/bmp"
              placeholder="Загрузить аватар"
              prepend-icon="mdi-camera"
              label="Изменение аватара"
            />
            <v-btn color="primary" class="mt-5">Применить</v-btn>
          </v-card>
        </v-tab-item>
        <v-tab-item>
          <profile-user-card :user="userCards.mikheev" />
        </v-tab-item>
        <v-tab-item>
          <profile-user-card :user="userCards.pauchkov" />
        </v-tab-item>
        <v-tab-item>
          <v-card flat width="500" class="mx-auto my-10">
            <ul class="text-caption">
              <li v-for="(group, index) of groups" :key="index">{{ group }}</li>
            </ul>
          </v-card>
        </v-tab-item>
        <v-tab-item>
          <v-card flat width="500" class="mx-auto my-10">
            <v-list>
              <v-list-item v-for="(relation, index) of relations" :key="index">
                <v-card-title>{{ relation }}</v-card-title>
              </v-list-item>
            </v-list>
          </v-card>
        </v-tab-item>
      </v-tabs>

    </v-card>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue';
import ProfilePasswordForm from './ProfilePasswordForm.vue';
import ProfileUserCard from './ProfileUserCard.vue';

export default Vue.extend({
  name: 'Profile',
  components: { 
    ProfileUserCard, 
    ProfilePasswordForm 
  },
  data() {
    return {
      loading: false,
      user: null,
      groups: null,
      relations: null,

      rules: {
        fileSize: (value: File) => !value
          || value.size < 2000000
          || 'Размер аватара дожен быть меньше, чем 2MB',
      },

      userCards: {
        zliminov: {
          email: 'sszliminoc@mail.com',
          phone: '8-987-654-32-10',
          department: 'The department',
          tabNumber: 33,
          title: ''
        },
        mikheev: {
          email: 'amikheev@mail.com',
          phone: '8-111-222-33-44',
          department: 'Hepartment',
          tabNumber: 34,
          title: ''
        },
        pauchkov: {
          email: 'pauchkov@mail.com',
          phone: '8-123-456-89-90',
          department: 'Pepardment',
          tabNumber: 34,
          title: ''
        },
      },
    }
  },

  methods: {
    getDataFromApi () {
      this.loading = true;
      this.$apiClient().then((client: any) => {
        client['profile-api-controller'].getProfileUsingPOST().then((data: any) => {
          const body = data.body;
          if (body) {
              this.user = body.user;
              this.groups = body.groups;
              this.relations = body.relations;
          }
          this.loading = false;
        });
      });
    },
  },

  mounted: function() {
    this.getDataFromApi();
  }
})
</script>
