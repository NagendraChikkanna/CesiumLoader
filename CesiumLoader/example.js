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

var location = Cesium.Cartesian3.fromDegrees(4.474182, 51.918438, 50); // Rotterdam
var camera = viewer.camera;

viewer.camera.flyTo({
    destination : location
});

var address = "";
var username = "";
var password = "";

loadScript(address + "/apps/cesiumloader/js/cesiumloader.js", function () {
    var loader = new BimServerCesiumLoader();
    loader.init(address, username, password, function(){
        var roids = [131075, 262147, 393219, 655363, 524291];
        roids.forEach(function(roid){
            loader.loadTypes(roid, "IfcSpace", function(boundingBox){
                var min = boundingBox.min;
                var max = boundingBox.max;

                var m = Cesium.Matrix4.multiplyByTranslation(
                Cesium.Transforms.eastNorthUpToFixedFrame(location), 
                new Cesium.Cartesian3(min.x / unitConversion, min.y / unitConversion, min.z / unitConversion), 
                new Cesium.Matrix4());

                var geometry = Cesium.BoxGeometry.fromDimensions({
                  vertexFormat : Cesium.VertexFormat.POSITION_AND_NORMAL,
                  dimensions : new Cesium.Cartesian3((max.x - min.x) / unitConversion, (max.y - min.y) / unitConversion, (max.z - min.z) / unitConversion)
                });
                var instance = new Cesium.GeometryInstance({
                  geometry : geometry,
                  modelMatrix : m,
                  attributes : {
                    color : Cesium.ColorGeometryInstanceAttribute.fromColor(Cesium.Color.AQUA)
                  },
                  id : boundingBox.guid
                });

                scene.primitives.add(new Cesium.Primitive({
                  geometryInstances : instance,
                  appearance : new Cesium.EllipsoidSurfaceAppearance({
                    material : Cesium.Material.fromType('Checkerboard')
                  })
                }));           
            });
        });
    });
});