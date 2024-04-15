<template>
  <div class="scrollable">
    <slot></slot>
  </div>
</template>

<style lang="scss">
  .scrollable {
    overflow-y: auto;
    overflow-x: hidden;
    height: 100vh;
  }
</style>
