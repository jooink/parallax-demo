/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the Creative Commons Attribution 3.0 Unported License.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Creative Commons Attribution 
 * 3.0 Unported License. for more details.
 * 
 * You should have received a copy of the the Creative Commons Attribution 
 * 3.0 Unported License along with Parallax. 
 * If not, see http://creativecommons.org/licenses/by/3.0/.
 */

package thothbot.parallax.demo.client.content.interactivity;

import java.util.List;

import thothbot.parallax.core.client.context.Canvas3d;
import thothbot.parallax.core.client.events.AnimationReadyEvent;
import thothbot.parallax.core.shared.Log;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.core.Color;
import thothbot.parallax.core.shared.core.Projector;
import thothbot.parallax.core.shared.core.Ray;
import thothbot.parallax.core.shared.core.Vector3;
import thothbot.parallax.core.shared.geometries.CubeGeometry;
import thothbot.parallax.core.shared.lights.DirectionalLight;
import thothbot.parallax.core.shared.materials.MeshLambertMaterial;
import thothbot.parallax.core.shared.objects.GeometryObject;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.demo.client.ContentWidget;
import thothbot.parallax.demo.client.Demo;
import thothbot.parallax.demo.client.DemoAnnotations.DemoSource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class InteractiveCubes extends ContentWidget 
{
	class Intersect
	{
		public GeometryObject object;
		public int currentHex;
	}

	/*
	 * Prepare Rendering Scene
	 */
	@DemoSource
	class DemoScene extends DemoAnimatedScene 
	{
		static final int radius = 100;
		
		PerspectiveCamera camera;
		
		Projector projector;
		
		double mouseDeltaX = 0, mouseDeltaY = 0;
		Intersect intersected;

		@Override
		protected void onStart()
		{
			camera = new PerspectiveCamera(
					70, // fov
					getRenderer().getAbsoluteAspectRation(), // aspect 
					1, // near
					10000 // far 
			);
			
			camera.getPosition().set( 0, 300, 500 );
			
			DirectionalLight light = new DirectionalLight( 0xffffff, 2 );
			light.getPosition().set( 1 ).normalize();
			getScene().add( light );

			DirectionalLight light2 = new DirectionalLight( 0xffffff );
			light2.getPosition().set( -1 ).normalize();
			getScene().add( light2 );

			CubeGeometry geometry = new CubeGeometry( 20, 20, 20 );

			for ( int i = 0; i < 2000; i ++ ) 
			{
				MeshLambertMaterial material = new MeshLambertMaterial();
				material.setColor(new Color( (int)(Math.random() * 0xffffff) ));
				Mesh object = new Mesh( geometry, material );

				object.getPosition().setX( Math.random() * 800 - 400 );
				object.getPosition().setY( Math.random() * 800 - 400 );
				object.getPosition().setZ( Math.random() * 800 - 400 );

				object.getRotation().setX( ( Math.random() * 360 ) * Math.PI / 180 );
				object.getRotation().setY( ( Math.random() * 360 ) * Math.PI / 180 );
				object.getRotation().setZ( ( Math.random() * 360 ) * Math.PI / 180 );

				object.getScale().setX( Math.random() + 0.5 );
				object.getScale().setY( Math.random() + 0.5 );
				object.getScale().setZ( Math.random() + 0.5 );

				getScene().add( object );

			}

			projector = new Projector();
			getRenderer().setClearColorHex(0xeeeeee);
			getRenderer().setSortObjects(false);
		}
		
		@Override
		protected void onUpdate(double duration)
		{
			Log.error(mouseDeltaX, mouseDeltaY);
			camera.getPosition().setX( radius * Math.sin( duration / 100 * Math.PI / 360 ) );
			camera.getPosition().setY( radius * Math.sin( duration / 100 * Math.PI / 360 ) );
			camera.getPosition().setZ( radius * Math.cos( duration / 100 * Math.PI / 360 ) );

			camera.lookAt( getScene().getPosition() );

			// find intersections

			Vector3 vector = new Vector3( mouseDeltaX, mouseDeltaY, 1 );
			projector.unprojectVector( vector, camera );

			Ray ray = new Ray( camera.getPosition(), vector.sub( camera.getPosition() ).normalize() );

			List<Ray.Intersect> intersects = ray.intersectObjects( getScene().getChildren() );

			if ( intersects.size() > 0 ) 
			{
				
				if ( intersected == null || intersected.object != intersects.get( 0 ).object ) 
				{
					if(intersected != null)
					{
						((MeshLambertMaterial)intersected.object.getMaterial()).getColor().setHex( intersected.currentHex );
					}
					
					intersected = new Intersect();
					intersected.object = (GeometryObject) intersects.get( 0 ).object;
					intersected.currentHex = ((MeshLambertMaterial)intersected.object.getMaterial()).getColor().getHex();
					((MeshLambertMaterial)intersected.object.getMaterial()).getColor().setHex( 0xff0000 );
				}
			}
			else 
			{
				if ( intersected != null ) 
					((MeshLambertMaterial)intersected.object.getMaterial()).getColor().setHex( intersected.currentHex );

				intersected = null;
			}
			
			getRenderer().render(getScene(), camera);
		}
	}
		
	public InteractiveCubes() 
	{
		super("Interactive cubes", "This example based on the three.js example.");
	}
	
	@Override
	public void onAnimationReady(AnimationReadyEvent event)
	{
		super.onAnimationReady(event);

		this.renderingPanel.getCanvas().addMouseMoveHandler(new MouseMoveHandler() {
		      @Override
		      public void onMouseMove(MouseMoveEvent event)
		      {
		    	  	DemoScene rs = (DemoScene) renderingPanel.getAnimatedScene();
		    	  	rs.mouseDeltaX = (event.getX() / (double)renderingPanel.getRenderer().getAbsoluteWidth() ) * 2.0 - 1.0; 
		    	  	rs.mouseDeltaY = - (event.getY() / (double)renderingPanel.getRenderer().getAbsoluteHeight() ) * 2.0 + 1.0;
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
		return Demo.resources.exampleInteractiveCubes();
	}
	
	@Override
	protected void asyncOnInitialize(final AsyncCallback<DemoAnimatedScene> callback)
	{
		GWT.runAsync(InteractiveCubes.class, new RunAsyncCallback() 
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
