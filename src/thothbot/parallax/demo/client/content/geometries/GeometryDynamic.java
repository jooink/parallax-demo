/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file based on the JavaScript source file of the THREE.JS project, 
 * licensed under MIT License.
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * Parallax. If not, see http://www.gnu.org/licenses/.
 */

package thothbot.parallax.demo.client.content.geometries;

import thothbot.parallax.core.client.RenderingPanel;
import thothbot.parallax.core.client.controls.FirstPersonControls;
import thothbot.parallax.core.client.gl2.enums.TextureWrapMode;
import thothbot.parallax.core.client.textures.Texture;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.Matrix4;
import thothbot.parallax.core.shared.geometries.PlaneGeometry;
import thothbot.parallax.core.shared.materials.MeshBasicMaterial;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.core.shared.scenes.FogExp2;
import thothbot.parallax.core.shared.utils.ImageUtils;
import thothbot.parallax.demo.client.ContentWidget;
import thothbot.parallax.demo.client.Demo;
import thothbot.parallax.demo.client.DemoAnnotations.DemoSource;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GeometryDynamic extends ContentWidget
{
	/*
	 * Prepare Rendering Scene
	 */
	@DemoSource
	class DemoScene extends DemoAnimatedScene 
	{
		private static final String texture = "./static/textures/water.jpg";
		
		FirstPersonControls controls;
		PlaneGeometry geometry;
		Mesh mesh;
		
		int worldWidth = 128;
		int worldDepth = 128;
		int worldHalfWidth = worldWidth / 2;
		int worldHalfDepth = worldDepth / 2;
		
		private double oldTime;
		
		@Override
		protected void loadCamera()
		{
			setCamera(
					new PerspectiveCamera(
							60, // fov
							getRenderer().getCanvas().getAspectRation(), // aspect 
							1, // near
							20000 // far 
					)); 
		}

		@Override
		protected void onStart()
		{
			getCamera().getPosition().setY(200);
			getScene().add(getCamera());

			getScene().setFog(new FogExp2( 0xAACCFF, 0.0007 ));

			this.controls = new FirstPersonControls( getCamera(), getRenderer().getCanvas() );
			controls.setMovementSpeed(500);
			controls.setLookSpeed(0.1);

			this.geometry = new PlaneGeometry( 20000, 20000, worldWidth - 1, worldDepth - 1 );
			this.geometry.applyMatrix(new Matrix4().makeRotationX( - Math.PI / 2.0 ));
			this.geometry.setDynamic( true );

			for ( int i = 0, il = this.geometry.getVertices().size(); i < il; i ++ )
				this.geometry.getVertices().get( i ).setY(35.0 * Math.sin( i/2.0 ));

			this.geometry.computeFaceNormals();
			this.geometry.computeVertexNormals();

			Texture texture = ImageUtils.loadTexture(DemoScene.texture);
			texture.setWrapS(TextureWrapMode.REPEAT); 
			texture.setWrapT(TextureWrapMode.REPEAT);
			texture.getRepeat().set( 5.0, 5.0 );

			MeshBasicMaterial material = new MeshBasicMaterial();
			material.setColor( new Color(0x0044ff) );
			material.setMap( texture );

			this.mesh = new Mesh( this.geometry, material );
			getScene().add( this.mesh );
			
			this.oldTime = Duration.currentTimeMillis();
		}
		
		@Override
		protected void onStop()
		{			
		}
		
		@Override
		protected void onUpdate(double duration)
		{			
			for ( int i = 0, l = this.geometry.getVertices().size(); i < l; i ++ )
				this.geometry.getVertices().get( i ).setY(35.0 * Math.sin( i / 5.0 + ( duration * 0.01 + i ) / 7.0 ));
		
			this.mesh.getGeometry().isVerticesNeedUpdate = true;
			
			this.controls.update( (Duration.currentTimeMillis() - this.oldTime) * 0.001);

			this.oldTime = Duration.currentTimeMillis();
		}
	}
		
	RenderingPanel renderingPanel;

	public GeometryDynamic()
	{
		super("Vertices moving", "Here are shown vertices moving on single surface and using dense fog. (left click: forward, right click: backward). This example based on the three.js example.");
	}
	
	@Override
	protected void loadRenderingPanelAttributes(RenderingPanel renderingPanel) 
	{
		super.loadRenderingPanelAttributes(renderingPanel);
		renderingPanel.setBackground(0xaaccff);
	}

	@Override
	public DemoScene onInitialize()
	{
		return new DemoScene();
	}

	@Override
	public ImageResource getIcon()
	{
		return Demo.resources.exampleGeometryDinamic();
	}
	
	@Override
	protected void asyncOnInitialize(final AsyncCallback<DemoAnimatedScene> callback)
	{
		GWT.runAsync(GeometryDynamic.class, new RunAsyncCallback() 
		{
			public void onFailure(Throwable caught)
			{
				callback.onFailure(caught);
			}

			public void onSuccess()
			{
				callback.onSuccess(onInitialize());
			}
		});
	}

}