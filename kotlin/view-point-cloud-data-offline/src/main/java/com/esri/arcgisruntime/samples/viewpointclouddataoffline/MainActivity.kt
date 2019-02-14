/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.samples.viewpointclouddataoffline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.esri.arcgisruntime.layers.PointCloudLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISScene
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Surface
import com.esri.arcgisruntime.mapping.view.Camera
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val permissionsRequestCode = 1
    private val _permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        sceneView.also { sceneView ->
            // create a camera and initial camera position
            sceneView.setViewpointCamera(Camera(32.7321157, -117.150072, 452.282774, 25.481533, 78.0945859, 0.0))

            // create a scene and add it to the scene view
            with(ArcGISScene(Basemap.createImagery())) {
                sceneView.scene = this

                // set the base surface with world elevation
                Surface().apply {
                    elevationSources.add(ArcGISTiledElevationSource(getString(R.string.elevation_source_url)))
                }.let { surface ->
                    // set the base surface of the scene
                    this.baseSurface = surface
                }
            }
        }

        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(this, _permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            createPointCloudLayer()
        } else {
            // request permission
            ActivityCompat.requestPermissions(this, _permissions, permissionsRequestCode)
        }
    }

    /**
     * Handle the permissions request response
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createPointCloudLayer()
        } else {
            // report to user that permission was denied
            Toast.makeText(this, resources.getString(R.string.read_permission_denied_message),
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun createPointCloudLayer() {
        // Add a point cloud layer to the scene by passing the URI of the Scene Layer Package to the constructor
        val pointCloudLayer = PointCloudLayer(
                Environment.getExternalStorageDirectory().toString() + getString(R.string.scene_layer_package_location))

        // Add a listener to perform operations when the load status of the PointCloudLayer changes
        pointCloudLayer.addLoadStatusChangedListener { loadStatusChangedEvent ->
            // When PointCloudLayer loads
            if (loadStatusChangedEvent.newLoadStatus == LoadStatus.LOADED) {
                // Add the PointCloudLayer to the Operational Layers of the Scene
                sceneView.scene.operationalLayers.add(pointCloudLayer)
            } else if (loadStatusChangedEvent.newLoadStatus == LoadStatus.FAILED_TO_LOAD) {
                // Notify user that the PointCloudLayer has failed to load
                Toast.makeText(this, R.string.point_cloud_layer_load_failure_message, Toast.LENGTH_LONG).show()
            }
        }

        // Load the PointCloudLayer asynchronously
        pointCloudLayer.loadAsync()
    }
}