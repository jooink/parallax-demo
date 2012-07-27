/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
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

package thothbot.parallax.demo.client.content;

import thothbot.parallax.core.client.AnimationReadyEvent;
import thothbot.parallax.core.client.RenderingPanel;
import thothbot.parallax.core.client.context.Canvas3d;
import thothbot.parallax.core.client.gl2.enums.PixelFormat;
import thothbot.parallax.core.client.textures.Texture;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.core.Color3f;
import thothbot.parallax.core.shared.geometries.Sphere;
import thothbot.parallax.core.shared.lights.PointLight;
import thothbot.parallax.core.shared.materials.MeshPhongMaterial;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.core.shared.utils.ImageUtils;
import thothbot.parallax.demo.client.ContentWidget;
import thothbot.parallax.demo.client.Demo;
import thothbot.parallax.demo.client.DemoAnnotations.DemoSource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class PerformanceDoubleSided extends ContentWidget 
{
	/*
	 * Prepare Rendering Scene
	 */
	@DemoSource
	class DemoScene extends DemoAnimatedScene 
	{
		private static final String textures = "./static/textures/cube/swedishRoyalCastle/*.jpg";
		
		public int mouseX;
		public int mouseY;
		
		@Override
		protected void loadCamera()
		{
			setCamera(
					new PerspectiveCamera(
							50, // fov
							getRenderer().getCanvas().getAspectRation(), // aspect 
							1, // near
							20000 // far 
					)); 
		}

		@Override
		protected void onStart()
		{
			getCamera().getPosition().setZ(3200);
			getScene().addChild(getCamera());
			
			PointLight light = new PointLight( 0x0011ff, 1, 5500 );
			light.getPosition().set( 4000, 0, 0 );
			getScene().addChild( light );

			PointLight light2 = new PointLight( 0xff1100, 1, 5500 );
			light2.getPosition().set( -4000, 0, 0 );
			getScene().addChild( light2 );

			PointLight light3 = new PointLight( 0xffaa00, 2, 3000 );
			light3.getPosition().set( 0, 0, 0 );
			getScene().addChild( light3 );

			Texture reflectionCube = ImageUtils.loadTextureCube( textures );
			reflectionCube.setFormat(PixelFormat.RGB);

			MeshPhongMaterial material = new MeshPhongMaterial();
			material.setSpecular( new Color3f(0xffffff) );
			material.setShininess( 100 );
			material.setEnvMap( reflectionCube );
			material.setCombine( Texture.OPERATIONS.MIX );
			material.setReflectivity( 0.1f );
			material.setPerPixel(true);
			material.setWrapAround(true); 
			material.getWrapRGB().set( 0.5f, 0.5f, 0.5f );

			Sphere geometry = new Sphere( 1, 32, 16, 0f, (float)Math.PI );

			for ( int i = 0; i < 5000; i ++ ) 
			{

				Mesh mesh = new Mesh( geometry, material );

				mesh.getPosition().setX( (float) (Math.random() * 10000.0 - 5000.0) );
				mesh.getPosition().setY( (float) (Math.random() * 10000.0 - 5000.0) );
				mesh.getPosition().setZ( (float) (Math.random() * 10000.0 - 5000.0) );

				mesh.getRotation().setX( (float) (Math.random() * 360.0 * ( Math.PI / 180.0 )) );
				mesh.getRotation().setY( (float) (Math.random() * 360.0 * ( Math.PI / 180.0 )) );
				
				float scale =  (float) (Math.random() * 50.0 + 100.0);
				mesh.getScale().set( scale );

				mesh.setMatrixAutoUpdate(false);
				mesh.updateMatrix();

				mesh.setDoubleSided(true);

				getScene().addChild( mesh );
			}

//			renderer.gammaInput = true;
//			renderer.gammaOutput = true;
//			renderer.physicallyBasedShading = true;
		}
		
		@Override
		protected void onStop()
		{			
		}
		
		@Override
		protected void onUpdate(double duration)
		{
			getCamera().getPosition().addX( ( mouseX - getCamera().getPosition().getX() ) * .05f );
			getCamera().getPosition().addY( ( - mouseY - getCamera().getPosition().getY() ) * .05f );

			getCamera().lookAt( getScene().getPosition() );
		}
	}
		
	public PerformanceDoubleSided() 
	{
		super("Double sided objects", "This example based on the three.js example.");
	}
	
	@Override
	protected void loadRenderingPanelAttributes(RenderingPanel renderingPanel) 
	{
		super.loadRenderingPanelAttributes(renderingPanel);
		renderingPanel.setBackground(0x050505);
	}
	
	@Override
	public void onAnimationReady(AnimationReadyEvent event)
	{
		super.onAnimationReady(event);

		this.renderingPanel.getRenderer().getCanvas().addMouseMoveHandler(new MouseMoveHandler() {
		      @Override
		      public void onMouseMove(MouseMoveEvent event)
		      {
		    	  	DemoScene rs = (DemoScene) renderingPanel.getAnimatedScene();
		    	  	Canvas3d canvas = renderingPanel.getRenderer().getCanvas();
		    	  	rs.mouseX = (event.getX() - canvas.getWidth() / 2 ) * 10; 
		    	  	rs.mouseY = (event.getY() - canvas.getHeight() / 2) * 10;
		      }
		});
	}
	
	@Override
	public DemoScene onInitialize()
	{
		return new DemoScene();
	}

	@Override
	public ImageResource getIcon()
	{
		return Demo.resources.examplePerformanceDoubleSided();
	}
	
	@Override
	protected void asyncOnInitialize(final AsyncCallback<DemoAnimatedScene> callback)
	{
		GWT.runAsync(PerformanceDoubleSided.class, new RunAsyncCallback() 
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
