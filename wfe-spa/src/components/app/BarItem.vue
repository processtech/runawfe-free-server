<script lang="ts">
  import Vue from 'vue';
  import { VHover, VListItem } from 'vuetify/lib';

  export default Vue.extend({
    name: 'AppBarItem' as string,
    render (h: any) {
      return h(VHover, {
        scopedSlots: {
          default: (hover: object) => {
            return h(VListItem, {
              attrs: this.$attrs,
              class: {
                'black--text': !hover,
                'white--text secondary elevation-12': hover,
              },
              props: {
                activeClass: '',
                dark: hover,
                link: true,
                ...this.$attrs,
              },
            }, this.$slots.default)
          },
        },
      })
    }
  });
</script>
