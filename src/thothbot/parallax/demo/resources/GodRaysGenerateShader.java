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

package thothbot.parallax.demo.resources;

import thothbot.parallax.core.client.shaders.Shader;
import thothbot.parallax.core.client.shaders.Uniform;
import thothbot.parallax.core.shared.math.Vector2;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.TextResource;

/**
 * The god-ray generation shader.
 * <p>
 * First pass:
 * <p>
 * The input is the depth map. I found that the output from the
 * THREE.MeshDepthMaterial material was directly suitable without
 * requiring any treatment whatsoever.
 * <p>
 * The depth map is blurred along radial lines towards the "sun". The
 * output is written to a temporary render target (I used a 1/4 sized
 * target).
 * <p>
 * Pass two & three:
 * <p>
 * The results of the previous pass are re-blurred, each time with a
 * decreased distance between samples.
 * <p>
 * The code from three.js project
 */
public final class GodRaysGenerateShader extends Shader 
{
	interface Resources extends DefaultResources
	{
		Resources INSTANCE = GWT.create(Resources.class);

		@Source("shaders/godrays.vs")
		TextResource getVertexShader();

		@Source("shaders/godraysGenerate.fs")
		TextResource getFragmentShader();
	}
	
	public GodRaysGenerateShader() 
	{
		super(Resources.INSTANCE);
	}

	@Override
	protected void initUniforms()
	{
		this.addUniform("tInput", new Uniform(Uniform.TYPE.T));
		this.addUniform("fStepSize", new Uniform(Uniform.TYPE.F, 1.0));
		this.addUniform("vSunPositionScreenSpace", new Uniform(Uniform.TYPE.V2, new Vector2( 0.5, 0.5 )));
	}
}
