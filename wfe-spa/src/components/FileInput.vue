<script setup lang="ts">
import { watch, type Ref } from 'vue'
import { toBase64 } from '@/logic/utils'
import { ref } from 'vue'

const SIZE_10MB = 10_000_000

const emit = defineEmits(['inputFiles'])

const files: Ref<File[]> = ref([])
const validators = ref([
  (files: File[]): boolean | string => validateSize(files) || 'Максимальный размер - 10MB',
])

watch(files, function provideFilesContext(files: File[]): void {
  Promise.all(files.map(toBase64))
    .then(fs => Object.assign({}, ...fs))
    .then(fs => emit('inputFiles', {
      files: fs,
      valid: validateSize(files),
      count: files.length,
    }))
})

function clear(): void {
  files.value = []
}

function browseFiles(): void {
  const input = document.querySelector('.v-field__field input[type=file]')
  if (input instanceof HTMLInputElement) {
    input.click()
  }
}

function validateSize(files: File[]): boolean {
  const totalSize = files.reduce((acc, f) => acc + f.size, 0)
  return totalSize <= SIZE_10MB
}

defineExpose({ clear })
</script>

<template>
  <div class="mx-5 d-flex align-center" style="width: 55%;">
    <v-file-input
      ref="fileInput"
      prepend-icon=""
      density="compact"
      chips
      multiple
      hide-details="auto"
      show-size
      small-chips
      v-model="files"
      :rules="validators"
      variant="underlined"
    />
    <v-btn class="mx-2" variant="text" icon="mdi-paperclip" @click="browseFiles" />
  </div>
</template>
