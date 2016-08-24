var viewer = new Cesium.Viewer('cesiumContainer');

function loadScript(url, callback) {
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;

    script.onreadystatechange = callback;
    script.onload = callback;

    head.appendChild(script);
}

var scene = viewer.scene;

var location = Cesium.Cartesian3.fromDegrees(4.474182, 51.918438, 0); // Rotterdam
var camera = viewer.camera;

var address = "http://localhost:8080";
var username = "admin@bimserver.org";
var password = "admin";

loadScript(address + "/apps/cesiumloader/js/cesiumloader.js", function () {
    var loader = new BimServerCesiumLoader();
    loader.init(address, username, password, function(){
        var roid = 720899;
        var query = {
          type: "IfcProduct",
          includeAllSubtypes: true,
          include: {
            type: "IfcProduct",
            field: "geometry",
            include: {
	        type: "GeometryInfo",
	        field: "data"
          }
        }
     };
     loader.loadGltf(roid, query, function(url){
            var entity = viewer.entities.add({
                position : location,
                model : {
                    uri : url,
                    scale: 0.001
                }
            });
            viewer.trackedEntity = entity;
        });
    });
});