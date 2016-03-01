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

var delft = Cesium.Cartesian3.fromDegrees(4.474182, 51.918438, 200);
var camera = viewer.camera;

viewer.camera.flyTo({
    destination : delft
});

	var aabb = Cesium.AxisAlignedBoundingBox.fromPoints(Cesium.Cartesian3.fromDegreesArray([
     -72.0, 40.0,
     -70.0, 35.0,
     -75.0, 30.0,
     -70.0, 30.0,
     -68.0, 40.0
]));
var box = Cesium.BoxOutlineGeometry.fromAxisAlignedBoundingBox(aabb);

var lastPos = null;

loadScript("http://localhost:8080/cesiumloader/js/cesiumloader.js", function () {
    var loader = new BimServerCesiumLoader();
    loader.init("http://localhost:8080", "admin@bimserver.org", "admin", function(){
        var roid = 65539;
        loader.loadTypes(roid, "IfcSpace", function(boundingBox){
            var min = boundingBox.min;
            var max = boundingBox.max;
            
            console.log(min);
            
            var pos = new Cesium.Cartesian3(min.x / 1000, min.y / 1000, min.z / 1000);
            var d = new Cesium.Cartesian3(0, 0, 0);
            Cesium.Cartesian3.add(delft, pos, d);
            
            lastPos = pos;
            
            var blueBox = viewer.entities.add({
                name : 'Blue box',
                position: d,
                box : {
                    dimensions : new Cesium.Cartesian3((max.x - min.x) / 1000, (max.y - min.y) / 1000, (max.z - min.z) / 1000),
                    material : Cesium.Color.BLUE
                }
            });
            
        });
    });
});