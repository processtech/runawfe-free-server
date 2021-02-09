<template>
  <section class="mb-12 text-center">
    <h1
      class="mb-2 text-h3"
      v-text="heading"
    />

    <div
      v-if="link"
      class="body-2 font-weight-light"
    >
      Please checkout the <a
        :href="`https://vuetifyjs.com/en/${link}`"
        class="text-decoration-none secondary--text"
        target="_blank"
      >full documentation</a>
    </div>

    <v-responsive
      class="text-center mx-auto text-body-1 font-weight-light"
      max-width="400"
    >
      <slot />
    </v-responsive>
  </section>
</template>

<script lang="ts">
  import Vue from 'vue'

  export default Vue.extend({
    name: 'ViewIntro' as string,

    props: {
      heading: String,
      link: String,
    },
  })
</script>
