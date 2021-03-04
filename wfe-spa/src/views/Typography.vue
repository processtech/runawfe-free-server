<template>
  <v-container
    id="typography-view"
    fluid
    tag="section"
  >
    <view-intro
      heading="Typography"
      link="styles/typography"
    />

    <v-row
      align="center"
      justify="center"
    >
      <v-col cols="12">
        <material-card
          color="success"
          full-header
        >
          <template #heading>
            <div class="pa-6 white--text">
              <div class="text-h4 font-weight-light">
                Material Dashboard Heading
              </div>

              <div class="text-subtitle-1">
                Created using the Roboto Font Family
              </div>
            </div>
          </template>

          <v-card-text class="text--primary">
            <v-row
              v-for="([name, text, component], key, i) in typography"
              :key="i"
              align="center"
            >
              <v-col
                cols="1"
                md="3"
                class="caption font-weight-regular text--disabled"
                v-text="name"
              />

              <v-col cols="8">
                <component
                  :is="component"
                  :class="key"
                >
                  <span
                    v-if="key !== 'blockquote'"
                    v-text="text"
                  />

                  <p
                    v-else
                    v-text="text"
                  />

                  <template v-if="key === 'small'">
                    <br>

                    <small>Use 'small' tag for the headers</small>
                  </template>
                </component>
              </v-col>
            </v-row>
          </v-card-text>
        </material-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
  import Vue from 'vue';
  // Data
  const leader = 'I will be the leader of a company that ends up being worth billions of dollars, because I got the answers. I understand culture. I am the nucleus. I think that’s a responsibility that I have, to push possibilities, to show people, this is the level that things could be at.'
  const leaderShort = leader.slice(0, 105) + '...'
  const material = 'The Life of Material Dashboard'
  const small = 'Header with small subtitle'

  export default Vue.extend({
    name: 'TypographyView' as string,

    data: () => ({
      typography: {
        'text-h1': ['Heading 1', material, 'h1'],
        'text-h2': ['Heading 2', material, 'h2'],
        'text-h3': ['Heading 3', material, 'h3'],
        'text-h4': ['Heading 4', material, 'h4'],
        'text-h5': ['Heading 5', material, 'h5'],
        'text-h6': ['Heading 6', material, 'h6'],
        '': ['Paragraph', leader, 'p'],
        blockquote: ['Quote', leader, 'blockquote'],
        'text--secondary': ['Muted Text', leaderShort, 'p'],
        'primary--text': ['Primary Text', leaderShort, 'p'],
        'info--text': ['Info Text', leaderShort, 'p'],
        'success--text': ['Success Text', leaderShort, 'p'],
        'warning--text': ['Warning Text', leaderShort, 'p'],
        'error--text': ['Error Text', leaderShort, 'p'],
        small: ['Small Tag', small, 'h2'],
      },
    }),
  });
</script>
