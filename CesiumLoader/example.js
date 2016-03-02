var viewer = new Cesium.Viewer('cesiumContainer');

function loadScript(url, callback)
{
    // Adding the script tag to the head as suggested before
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = url;

    // Then bind the event to the callback function.
    // There are several events for cross browser compatibility.
    script.onreadystatechange = callback;
    script.onload = callback;

    // Fire the loading
    head.appendChild(script);
}

var scene = viewer.scene;

var delft = Cesium.Cartesian3.fromDegrees(4.474182, 51.918438, 10);
var camera = viewer.camera;

viewer.camera.flyTo({
    destination : delft
});

var unitConvert = 1;

loadScript("http://localhost:8080/cesiumloader/js/cesiumloader.js", function () {
    var loader = new BimServerCesiumLoader();
    loader.init("http://localhost:8080", "admin@bimserver.org", "admin", function(){
//        var roid = 65539;
        var roid = 131075;
        loader.loadTypes(roid, "IfcSpace", function(boundingBox){
            var min = boundingBox.min;
            var max = boundingBox.max;

            var m = Cesium.Matrix4.multiplyByTranslation(
                Cesium.Transforms.eastNorthUpToFixedFrame(delft), 
                new Cesium.Cartesian3(min.x / unitConvert, min.y / unitConvert, min.z / unitConvert), 
                new Cesium.Matrix4());

                var geometry = Cesium.BoxGeometry.fromDimensions({
              vertexFormat : Cesium.VertexFormat.POSITION_AND_NORMAL,
              dimensions : new Cesium.Cartesian3((max.x - min.x) / unitConvert, (max.y - min.y) / unitConvert, (max.z - min.z) / unitConvert)
            });
            var instance = new Cesium.GeometryInstance({
              geometry : geometry,
              modelMatrix : m,
              attributes : {
                color : Cesium.ColorGeometryInstanceAttribute.fromColor(Cesium.Color.BLUE)
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