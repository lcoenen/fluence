module.exports = {
  "entry": {
    "kademlia-http-opt": ["/home/diemust/git/fluence/kademlia/http/js/target/scala-2.12/scalajs-bundler/main/kademlia-http-opt-entrypoint.js"]
  },
  "node": {
    "fs": 'empty'
  },
  "output": {
    "path": "/home/diemust/git/fluence/kademlia/http/js/target/scala-2.12/scalajs-bundler/main",
    "filename": "[name]-library.js",
    "library": "ScalaJSBundlerLibrary",
    "libraryTarget": "var"
  },
  "devtool": "source-map",
  "module": {
    "rules": [{
      "test": new RegExp("\\.js$"),
      "enforce": "pre",
      "use": ["source-map-loader"]
    }]
  }
}