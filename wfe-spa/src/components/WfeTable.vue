<template>
  <section id="wfe-table">
    <slot />
  </section>
</template>

<style lang="scss">
  #wfe-table {
    & {
      table {
        > tbody > tr {
          cursor: default;
          > td {
            border: none;
          }
        }
      }
    }
    .v-data-table-footer {
      scale: 0.9;
      color: rgb(var(--v-theme-primary-text));
    }
  }
</style>
