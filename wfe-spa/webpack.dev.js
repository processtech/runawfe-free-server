const path = require('path');
const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');

module.exports = merge(common, {
  mode: 'development',
  devtool: 'inline-source-map', // For debuging source code
  devServer: {
    open: true,
    index: 'index.html',
    // writeToDisk: true,
    contentBase: path.join(__dirname, 'dist'),
    hot: true, // Enabling Hot Module Replacement
    host: 'localhost',
    port: 3000,
    overlay: {
      warnings: true,
      errors: true
    },
    disableHostCheck: true,
  },
  module: {
    rules: [
      {
        test: /\.(sa|sc|c)ss$/,
        use: [
          'vue-style-loader',
          {
            loader: 'css-loader', 
            options: {
              sourceMap: true, // Enables generation of source maps
            },
          },
          {
            loader: 'sass-loader', 
            options: {
              implementation: require('sass'),
              sourceMap: true,
              sassOptions: {
                indentedSyntax: true
              },
              additionalData: "@import './src/styles/variables.scss'",
            },
          },
        ],
      },
    ]
  },
});