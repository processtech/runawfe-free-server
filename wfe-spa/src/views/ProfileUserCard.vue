<template>
  <v-card flat width="600" class="mx-auto my-10">
    <v-card-title>{{ user.title }}</v-card-title>
    <v-container class="text-caption">
      <v-row>
        <v-col>
          <v-container>
            <v-row>
              <v-col>E-mail:</v-col>
              <v-col>
                <strong>{{ user.email }}</strong>
              </v-col>
            </v-row>
            <v-row>
              <v-col>Телефон:</v-col>
              <v-col><strong>{{ user.phone }}</strong></v-col>
            </v-row>
          </v-container>
        </v-col>
        <v-col>
          <v-container>
            <v-row>
              <v-col>Отдел:</v-col>
              <v-col><strong>{{ user.department }}</strong></v-col>
            </v-row>
            <v-row>
              <v-col>Табельный номер:</v-col>
              <v-col><strong>{{ user.code }}</strong></v-col>
            </v-row>
          </v-container>
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
export default {
  props: [ 'user' ],
}
</script>

<style>

</style>
