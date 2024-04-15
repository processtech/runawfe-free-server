<template>
  <div class="bg-primary-background">
    <task-summary :task="task" />
    <v-row>
      <v-col cols="12">
        <iframe
          :src="oldFormUrl"
          width="100%"
          height="100%"
          frameborder="0"
          style="min-height: 317px;">
          Ваш браузер не поддерживает плавающие фреймы!
        </iframe>
      </v-col>
    </v-row>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import type { WfeTask } from '../ts/WfeTask'
import { mapState } from 'pinia'
import { useSystemStore } from '../stores/system-store'
import { useAuthStore } from '../stores/auth-store'
import TaskSummary from '../components/TaskSummary.vue'
import { taskService } from '../services/task-service'

export default defineComponent({
  components: { TaskSummary },
  name: 'TaskCard',

  data: () => ({
    showInfo: false,
    task: {} as WfeTask,
    oldFormUrl: 'about:blank',
  }),

  computed: {
    ...mapState(useSystemStore, ['serverUrl']),
    ...mapState(useAuthStore, ['token']),
  },

  created: function() {
    taskService.getTask(Number(this.$route.params.id))
      .then(t => this.task = t)
      .then(() => this.oldFormUrl = `${this.serverUrl}/wfe/newweboldform.do?id=${this.task.id}&title=${encodeURIComponent(this.task.name)}&jwt=${this.token}&startForm=false`)
  }
});
</script>
