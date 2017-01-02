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

var position = Cesium.Cartesian3.fromDegrees(4.4888735, 51.9066471, 150);
var camera = viewer.camera;

var address = "http://localhost:8080";
var username = "admin@bimserver.org";
var password = "admin";

loadScript(address + "/apps/cesiumloader/js/cesiumloader.js", function () {
    var loader = new BimServerCesiumLoader();
    loader.init(address, username, password, function(){
        var roid = 327683;
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
    var heading = Cesium.Math.toRadians(143);
    var hpr = new Cesium.HeadingPitchRoll(heading, 0, 0);
    var orientation = Cesium.Transforms.headingPitchRollQuaternion(position, hpr);
        
     loader.loadGltf(roid, query, function(url){
            var entity = viewer.entities.add({
                position : position,
                orientation : orientation,
                model : {
                    uri : url,
                    scale: 0.001
                }
            });
            viewer.trackedEntity = entity;
        });
    });
});