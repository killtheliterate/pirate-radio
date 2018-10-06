const HtmlWebpackPlugin = require('html-webpack-plugin');
const HtmlWebpackIncludeAssetsPlugin = require('html-webpack-include-assets-plugin');

config.output.publicPath = "";
config.plugins.push(new HtmlWebpackPlugin({
    title: Object.keys(config.entry)[0]
}));

config.plugins.push(new HtmlWebpackIncludeAssetsPlugin({
    assets: ["https://cdnjs.cloudflare.com/ajax/libs/peerjs/0.3.14/peer.js"],
    publicPath: "",
    append: false
}));