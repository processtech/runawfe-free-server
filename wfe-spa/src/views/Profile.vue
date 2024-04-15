<template>
  <v-card :loading="loading" class="text-primary-text">
    <v-toolbar flat color="transparent" class="px-3 py-1">
      <v-icon x-large>mdi-account</v-icon>
      <v-container v-if="user" class="mx-3">
        <v-toolbar-title>{{ user.name }}</v-toolbar-title>
        <p class="text-caption">{{ user.description }}</p>
      </v-container>
    </v-toolbar>
    <div class="d-flex flex-row">
      <v-tabs direction="vertical" v-model="tab" color="primary" class="text-medium-emphasis">
        <v-tab v-for="item in menuItems" :key="item.to" :value="item.to">
          <v-icon>{{ item.icon }}</v-icon>
          <span class="px-2">{{ item.title }}</span>
        </v-tab>
      </v-tabs>
      <v-window v-model="tab" class="mx-auto text-primary-text">
        <v-window-item value="account-details">
          <profile-user-card v-if="user" :user="user"/>
        </v-window-item>
        <v-window-item value="change-password">
          <profile-password-form />
        </v-window-item>
        <v-window-item value="substitutions">
          <!-- Find out how to get users from substitutions, if it's possible -->
        </v-window-item>
        <v-window-item value="may-substitute">
          <!-- The same -->
        </v-window-item>
        <v-window-item value="groups">
          <v-card flat width="500" class="mx-auto my-10">
            <v-list>
              <v-list-item v-for="(group, index) of groups" :key="index" two-line>
                <v-list-item-title class="font-weight-bold">{{ group.name }}</v-list-item-title>
                  <v-list-item-subtitle class="text-caption">
                    {{ group.description }}
                  </v-list-item-subtitle>
                  <v-divider />
              </v-list-item>
            </v-list>
          </v-card>
        </v-window-item>
        <v-window-item value="relations">
          <v-card flat width="500" class="mx-auto my-10">
            <v-list>
              <v-list-item v-for="(relation, index) of relations" :key="index" two-line>
                <v-list-item-title class="font-weight-bold">{{ relation.name }}</v-list-item-title>
                  <v-list-item-subtitle class="text-caption">
                    {{ relation.description }}
                  </v-list-item-subtitle>
                  <v-divider />
              </v-list-item>
            </v-list>
          </v-card>
        </v-window-item>
        <v-window-item value="ui-preferences">
          <v-list>
            <v-list-item>
              <v-checkbox
                density="compact"
                v-model="startFormPreference"
                label="Показывать стартовую форму процесса в том же окне"
                color="accent"
              />
            </v-list-item>
            <v-list-item v-if="isAdmin">
              <v-checkbox
                density="compact"
                v-model="editVariablesPreference"
                label="Редактировать переменные"
                color="accent"
              />
            </v-list-item>
          </v-list>
        </v-window-item>
      </v-window>
    </div>
  </v-card>
</template>

<script lang="ts">
import { mapActions, mapState } from 'pinia';
import { defineComponent } from 'vue';
import { executorService } from '../services/executor-service';
import { usePreferencesStore } from '../stores/preferencese-store';
import { useSystemStore } from '../stores/system-store';
import { MenuItem } from '../ts/menu-item';
import ProfilePasswordForm from './ProfilePasswordForm.vue';
import ProfileUserCard from './ProfileUserCard.vue';
import { profileMenuItems } from '../static/profile-menu-items'

export default defineComponent({
  name: 'Profile',
  components: {
    ProfileUserCard,
    ProfilePasswordForm
  },
  data() {
    return {
      loading: true,
      user: {}, // TODO rename to profile
      groups: [],
      relations: [],
      isAdmin: false,
      menuItems: [] as MenuItem[],
      rules: {
        fileSize: (value: File) => !value
          || value.size < 2000000
          || 'Размер аватара дожен быть меньше, чем 2MB',
      },
    }
  },

  computed: {
    ...mapState(usePreferencesStore, ['processStartFormByRowClick', 'editVariables']),
    ...mapState(useSystemStore, ['serverUrl']),

    tab: {
      get(): string {
        return this.$route.query.tab
      },
      set(tab: string): void {
        this.$router.push({ query: { tab }})
      },
    },

    startFormPreference: {
      get(): boolean {
        return this.processStartFormByRowClick
      },
      set(): void {
        this.toggleStartFormByRowClick()
      },
    },

    editVariablesPreference: {
      get(): boolean {
        return this.editVariables
      },
      set(): void {
        this.toggleEditVariables()
      },
    },
  },

  methods: {
    ...mapActions(usePreferencesStore, ['toggleStartFormByRowClick', 'toggleEditVariables']),

    getUserData() {
      this.$apiClient().then((client: any) => {
        client['profile-controller'].getProfileUsingGET().then((data: any) => {
          const body = data.body;
          if (body) {
            this.user = body.user;
            this.groups = body.groups;
            this.relations = body.relations;
            executorService.isAdministrator(this.user.id)
              .then(isAdmin => this.isAdmin = isAdmin)
          }
          this.loading = false;
        });
      });
    },

    toAdminUi(): void {
      window.location.href = this.serverUrl + '/wfe'
    },
  },

  mounted: function() {
    this.getUserData();
    this.menuItems = profileMenuItems
    this.tab = this.$route.query.tab || 'account-details'
  }
})
</script>
