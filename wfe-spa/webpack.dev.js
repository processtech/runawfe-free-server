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
        test: /\.((c|sa|sc)ss)$/i,
        use: [
          'vue-style-loader', 
          'css-loader', 
          {
            loader: 'sass-loader', 
            options: {
              implementation: require('sass'),
              sassOptions: {
                indentedSyntax: true
              },
            },
          },
        ],
      },
    ]
  },
});