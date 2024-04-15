<!-- TODO use kebab case naming slots -->
<template>
  <v-toolbar flat class="bg-primary-background-darken-1">
    <v-spacer/>
    <v-btn icon="mdi-reload" @click="$emit('reload')" />
    <slot name="filterControl" />
    <slot name="columnsControl" />
    <slot name="variablesControl" />
    <slot name="colorsControl" />
  </v-toolbar>
</template>
