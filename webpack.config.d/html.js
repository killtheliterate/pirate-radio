const HtmlWebpackPlugin = require('html-webpack-plugin');

config.output.publicPath = "/pirate-radio";
config.plugins.push(new HtmlWebpackPlugin({
    title: Object.keys(config.entry)[0]
}));