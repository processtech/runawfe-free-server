<template>
    <v-footer
      id="default-footer"
      color="transparent"
      absolute
      app
      inset
    >
        <v-container>
            <v-row align="center">
                <v-col
                    cols="12"
                    md="auto"
                >
                    <div class="body-1 font-weight-light pt-6 pt-md-0 text-center">
                    &copy; {{ (new Date()).getFullYear() }}, Runawfe Professional
                    </div>
                </v-col>
            </v-row>
        </v-container>
    </v-footer>
</template>

<script lang="ts">
import Vue from 'vue'
export default Vue.extend({
    name: 'DefaultFooter',
})
</script>