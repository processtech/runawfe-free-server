<template>
  <div>
    <v-file-input v-if="editing" />
    <span v-else @click="download">
      <v-chip class="file-chip" link>
        <v-icon>mdi-tray-arrow-down</v-icon>
        <span>{{ truncateLabel(variable.value.name, 25) }}</span>
      </v-chip>
    </span>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { truncateLabel } from '../../logic/utils'
import { processService } from '../../services/process-service'
import { WfeVariable } from '../../ts/WfeVariable'

export default defineComponent({
  name: 'FileVariableFormat',

  props: {
    variable: Object as PropType<WfeVariable>,
    editing: {
      type: Boolean,
      default: false,
    },
  },

  methods: {
    truncateLabel,

    download(): void { // TODO move the logic to utils
     processService.getVariableFile(this.$route.params.id, this.variable.name).then(blob => {
       const link = document.createElement('a');
       link.href = URL.createObjectURL(blob);
       link.download = this.variable.value.name;
       link.click();
       URL.revokeObjectURL(link.href);
     });
   },
  },
})
</script>
