const path = require('path');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const VueLoaderPlugin = require('vue-loader/lib/plugin');
const VuetifyLoaderPlugin = require('vuetify-loader/lib/plugin');

module.exports = {
  entry: {
      index: './src/index.ts', // Главный файл js, который включает все остальные js модули, а во время сборки подключается в выходной index.html
  },
  output: {
    filename: 'assets/js/[name].[contenthash].js',
    path: path.resolve(__dirname, 'dist'),
  },
  plugins: [
    new CleanWebpackPlugin(), // Очищает папку dist перед каждой сборкой
    new HtmlWebpackPlugin({ // Генерирует файл ~/dist/index.html на базе шаблона ~src/index.html и включает ~src/index.ts перед закрывающим тегом </body>.
      title: 'RunaWFE Professional',
      filename: 'index.html',
      template: 'src/index.html',
    }),
    new VueLoaderPlugin(), // Обеспечивает работу однофайловых компонентов
    new VuetifyLoaderPlugin(), // Управляет автозагрузкой компонентов Vuetify, хотя существует и ручной способ, но более сложный
  ],
  module: {
    // Конфигурации загрузчиков файлов с правилами обработки, компиляции/транспиляции/минификации
    rules: [
      {
        test: /\.ts$/,
        use: [
          {
            loader: 'ts-loader',
            options: {
              transpileOnly: true,
              appendTsSuffixTo: [/\.vue$/]
            }
          }
        ],
        exclude: /node_modules/,
      },
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          esModule: true
        }
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: 'asset/resource',
        generator: {
          filename: 'assets/images/[hash][ext][query]'
        },
      },
      {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: 'asset/resource',
        generator: {
          filename: 'assets/fonts/[hash][ext][query]'
        },
      },
    ],
  },
  resolve: {
    // Обрабатываемые расширения.
    extensions: [ '.ts', '.js', '.vue', '.json' ],
  },
};