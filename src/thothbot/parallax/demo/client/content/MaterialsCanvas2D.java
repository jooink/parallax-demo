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

package thothbot.parallax.demo.client.content;

import java.util.ArrayList;
import java.util.List;

import thothbot.parallax.core.client.textures.Texture;
import thothbot.parallax.core.shared.cameras.PerspectiveCamera;
import thothbot.parallax.core.shared.core.Color3f;
import thothbot.parallax.core.shared.core.Face3;
import thothbot.parallax.core.shared.core.Geometry;
import thothbot.parallax.core.shared.core.Vector3f;
import thothbot.parallax.core.shared.geometries.Sphere;
import thothbot.parallax.core.shared.lights.AmbientLight;
import thothbot.parallax.core.shared.lights.DirectionalLight;
import thothbot.parallax.core.shared.lights.PointLight;
import thothbot.parallax.core.shared.materials.LineBasicMaterial;
import thothbot.parallax.core.shared.materials.Material;
import thothbot.parallax.core.shared.materials.MeshBasicMaterial;
import thothbot.parallax.core.shared.materials.MeshDepthMaterial;
import thothbot.parallax.core.shared.materials.MeshFaceMaterial;
import thothbot.parallax.core.shared.materials.MeshLambertMaterial;
import thothbot.parallax.core.shared.materials.MeshNormalMaterial;
import thothbot.parallax.core.shared.materials.MeshPhongMaterial;
import thothbot.parallax.core.shared.objects.Line;
import thothbot.parallax.core.shared.objects.Mesh;
import thothbot.parallax.demo.client.ContentWidget;
import thothbot.parallax.demo.client.Demo;
import thothbot.parallax.demo.client.DemoAnnotations.DemoSource;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class MaterialsCanvas2D extends ContentWidget 
{

	/*
	 * Prepare Rendering Scene
	 */
	@DemoSource
	class DemoScene extends DemoAnimatedScene 
	{
		
		private List<Material> materials;
		private PointLight pointLight;
		private Mesh particleLight;
		private List<Mesh> objects;
		
		@Override
		protected void loadCamera()
		{
			setCamera(
					new PerspectiveCamera(
							45, // fov
							getRenderer().getCanvas().getAspectRation(), // aspect 
							1, // near
							2000 // far 
					)); 
		}

		@Override
		protected void onStart()
		{
			getCamera().getPosition().set(0, 200, 800);
			getScene().addChild(getCamera());
			
			// Grid

			Geometry geometry = new Geometry();
			
			float floor = -75, step = 25;

			for ( int i = 0; i <= 40; i ++ ) 
			{
				geometry.getVertices().add( new Vector3f( - 500f, floor, i * step - 500f ) );
				geometry.getVertices().add( new Vector3f(   500f, floor, i * step - 500f ) );

				geometry.getVertices().add( new Vector3f( i * step - 500f, floor, -500f ) );
				geometry.getVertices().add( new Vector3f( i * step - 500f, floor,  500f ) );
			}

			LineBasicMaterial line_material = new LineBasicMaterial();
			line_material.setColor( new Color3f( 0xffffff) );
			line_material.setOpacity( 0.2f );

			Line line = new Line( geometry, line_material, Line.TYPE.PIECES );
			getScene().addChild( line );

			// Materials

			Texture texture = new Texture( generateTexture() );

			texture.setNeedsUpdate(true);

			this.materials = new ArrayList<Material>();
			
			MeshLambertMaterial mlOpt = new MeshLambertMaterial();
			mlOpt.setMap(texture);
			mlOpt.setTransparent(true);
			materials.add( mlOpt );
			
			MeshLambertMaterial mlOpt1 = new MeshLambertMaterial();
			mlOpt1.setColor( new Color3f(0xdddddd) );
			mlOpt1.setShading( Material.SHADING.FLAT );
			materials.add( mlOpt1 );
			
			MeshPhongMaterial mpOpt = new MeshPhongMaterial();
			mpOpt.setAmbient( new Color3f(0x030303) );
			mpOpt.setColor( new Color3f(0xdddddd) );
			mpOpt.setSpecular( new Color3f(0x009900) );
			mpOpt.setShininess( 30f );
			mpOpt.setShading( Material.SHADING.FLAT );
			materials.add( mpOpt );
			
			materials.add( new MeshNormalMaterial() );
			
			MeshBasicMaterial mbOpt = new MeshBasicMaterial();
			mbOpt.setColor( new Color3f(0xffaa00) );
			mbOpt.setTransparent( true );
			mbOpt.setBlending( Material.BLENDING.ADDITIVE );
			materials.add( mbOpt );

			MeshLambertMaterial mlOpt2 = new MeshLambertMaterial();
			mlOpt2.setColor( new Color3f(0xdddddd) );
			mlOpt2.setShading( Material.SHADING.SMOOTH );
			materials.add( mlOpt2 );
			
			MeshPhongMaterial mpOpt2 = new MeshPhongMaterial();
			mpOpt2.setMap( texture );
			mpOpt2.setTransparent( true );			
			materials.add( mpOpt2 );
			
			MeshNormalMaterial mnOpt = new MeshNormalMaterial();
			mnOpt.setShading( Material.SHADING.SMOOTH );
			materials.add( mnOpt );
			
			MeshBasicMaterial mbOpt1 = new MeshBasicMaterial();
			mbOpt1.setColor( new Color3f(0x00ffaa) );
			mbOpt1.setWireframe(true);
			materials.add( mbOpt1 );

			materials.add( new MeshDepthMaterial() );

			MeshLambertMaterial mlOpt3 = new MeshLambertMaterial();
			mlOpt3.setColor( new Color3f(0x666666) );
			mlOpt3.setEmissive( new Color3f(0xff0000) );
			mlOpt3.setAmbient( new Color3f(0x000000) );
			mlOpt3.setShading( Material.SHADING.SMOOTH );
			materials.add( mlOpt3 );
			
			MeshPhongMaterial mpOpt1 = new MeshPhongMaterial();
			mpOpt1.setAmbient( new Color3f(0x000000) );
			mpOpt1.setEmissive( new Color3f(0xffff00) );
			mpOpt1.setColor( new Color3f(0x000000) );
			mpOpt1.setSpecular( new Color3f(0x666666) );
			mpOpt1.setShininess( 10f );
			mpOpt1.setShading( Material.SHADING.SMOOTH );
			mpOpt1.setOpacity( 0.9f );
			mpOpt1.setTransparent(true);
			materials.add( mpOpt1 );

			MeshBasicMaterial mbOpt2 = new MeshBasicMaterial();
			mbOpt1.setMap( texture );
			mbOpt1.setTransparent( true );
			materials.add( mbOpt2 );

			// Spheres geometry

			Sphere geometry_smooth = new Sphere( 70, 32, 16 );
			Sphere geometry_flat = new Sphere( 70, 32, 16 );
			Sphere geometry_pieces = new Sphere( 70, 32, 16 ); // Extra geometry to be broken down for MeshFaceMaterial

			for ( int i = 0, l = geometry_pieces.getFaces().size(); i < l; i ++ ) 
			{
				Face3 face = geometry_pieces.getFaces().get( i );

//				if ( Math.random() > 0.7 )
//					face.setMaterialIndex( (int)( Math.random() * materials.size()) );

//				else
					face.setMaterialIndex( 0 );

			}

			geometry_pieces.setMaterials(materials);

			materials.add( new MeshFaceMaterial() );

			this.objects = new ArrayList<Mesh>();

			for ( int i = 0, l = materials.size(); i < l; i ++ ) 
			{
				Material material = materials.get( i );

				Geometry geometryMesh = material.getClass() == MeshFaceMaterial.class 
							? geometry_pieces 
							: ( material.getShading() == Material.SHADING.FLAT 
								? geometry_flat 
								: geometry_smooth );

				Mesh sphere = new Mesh( geometryMesh, material );

				sphere.getPosition().setX(( i % 4 ) * 200f - 400f);
				sphere.getPosition().setZ((float) (Math.floor( i / 4.0 ) * 200f - 200f));

				sphere.getRotation().setX((float) (Math.random() * 200.0 - 100.0));
				sphere.getRotation().setY((float) (Math.random() * 200.0 - 100.0));
				sphere.getRotation().setZ((float) (Math.random() * 200.0 - 100.0));

				this.objects.add( sphere );

				getScene().addChild( sphere );

			}
			
			MeshBasicMaterial mbOpt3 = new MeshBasicMaterial();
			mbOpt3.setColor( new Color3f(0xffffff) );
			this.particleLight = new Mesh( new Sphere( 4, 8, 8 ), mbOpt3 );
			getScene().addChild( this.particleLight );

			// Lights

			getScene().addChild( new AmbientLight( 0x111111 ) );

			DirectionalLight directionalLight = new DirectionalLight( 0xffffff, 0.125f );

			directionalLight.getPosition().setX( (float) (Math.random() - 0.5) );
			directionalLight.getPosition().setY( (float) (Math.random() - 0.5) );
			directionalLight.getPosition().setZ( (float) (Math.random() - 0.5) );

			directionalLight.getPosition().normalize();

			getScene().addChild( directionalLight );

			this.pointLight = new PointLight( 0xffffff );
			getScene().addChild( pointLight );
		}
		
		private CanvasElement generateTexture() 
		{
			CanvasElement canvas = Document.get().createElement("canvas").cast();
			canvas.setWidth(256);
			canvas.setHeight(256);

			Context2d context = canvas.getContext2d();
			ImageData image = context.getImageData( 0, 0, 256, 256 );

			int x = 0, y = 0;
			for ( int i = 0, j = 0, l = image.getData().getLength(); i < l; i += 4, j ++ ) 
			{
				x = j % 64;
				y = x == 0 ? y + 1 : y;

				image.getData().set( i, 255);
				image.getData().set( i + 1, 255);
				image.getData().set( i + 2, 255);
				image.getData().set( i + 3, (int)Math.floor( x ^ y ));
			}

			context.putImageData( image, 0, 0 );

			return canvas;
		}
		
		@Override
		protected void onStop()
		{			
		}
		
		@Override
		protected void onUpdate(double duration)
		{
			double timer = 0.0001 * duration;

			getCamera().getPosition().setX( (float) (Math.cos( timer ) * 1000.0) );
			getCamera().getPosition().setZ( (float) (Math.sin( timer ) * 1000.0) );

			getCamera().lookAt( getScene().getPosition() );

			for ( int i = 0, l = this.objects.size(); i < l; i ++ ) 
			{
				Mesh object = this.objects.get(i);

				object.getRotation().addX(0.01f);
				object.getRotation().addY(0.005f);

				Material material = this.materials.get( i ); 
				if(i > 9 && material instanceof MeshPhongMaterial)
				{
					((MeshPhongMaterial)material).getEmissive()
						.setHSV( 0.54f, 1.0f, (float) (0.7 * ( 0.5 + 0.5 * Math.sin( 35 * timer ) )) );	
				}
				else if(i > 9 && material instanceof MeshLambertMaterial)
				{
					((MeshLambertMaterial)material).getEmissive()
						.setHSV( 0.04f, 1.0f, (float) (0.7 * ( 0.5 + 0.5 * Math.cos( 35 * timer ) )) );	
				}
			}
			
			this.particleLight.getPosition().setX( (float) (Math.sin( timer * 7 ) * 300.0) );
			this.particleLight.getPosition().setY( (float) (Math.cos( timer * 5 ) * 400.0) );
			this.particleLight.getPosition().setZ( (float) (Math.cos( timer * 3 ) * 300.0));

			this.pointLight.getPosition().setX( particleLight.getPosition().getX() );
			this.pointLight.getPosition().setY( particleLight.getPosition().getY() );
			this.pointLight.getPosition().setZ( particleLight.getPosition().getZ() );
		}
	}
		
	public MaterialsCanvas2D() 
	{
		super("Canvas 2D texture", "This example based on the three.js example.");
	}
	
	@Override
	public DemoScene onInitialize()
	{
		return new DemoScene();
	}

	@Override
	public ImageResource getIcon()
	{
		return Demo.resources.exampleMaterialsCanvas2D();
	}
	
	@Override
	protected void asyncOnInitialize(final AsyncCallback<DemoAnimatedScene> callback)
	{
		GWT.runAsync(MaterialsCanvas2D.class, new RunAsyncCallback() 
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
