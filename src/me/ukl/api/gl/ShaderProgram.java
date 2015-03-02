package me.ukl.api.gl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL20;

import com.mumfrey.liteloader.gl.GL;

public class ShaderProgram extends GLObject {
	
	public int prog;
	
	public ShaderProgram(String pathToVert, String pathToFrag) throws IOException {
		super(GL20.glCreateProgram());
		prog = getID();
		BufferedReader vr = getReader(pathToVert);
		BufferedReader fr = getReader(pathToFrag);
		
		int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		GL20.glShaderSource(vs, readFully(vr));
		GL20.glCompileShader(vs);
		GL20.glShaderSource(fs, readFully(fr));
		GL20.glCompileShader(fs);
		
		String l0 = GL20.glGetShaderInfoLog(vs, 65536);
		String l1 = GL20.glGetShaderInfoLog(fs, 65536);
		System.out.println("===VERTEX===");
		System.out.println(l0);
		System.out.println("===FRAGMENT===");
		System.out.println(l1);
		
		GL20.glAttachShader(prog, vs);
		GL20.glAttachShader(prog, fs);
		
		GL20.glBindAttribLocation(prog, 0, "a_pos");
		GL20.glBindAttribLocation(prog, 1, "a_tex");
		
		GL20.glLinkProgram(prog);
	}
	
	private static BufferedReader getReader(String path) {
		return new BufferedReader(new InputStreamReader(ShaderProgram.class.getResourceAsStream(path)));
	}
	
	private static String readFully(BufferedReader rdr) throws IOException {
		StringBuilder bldr = new StringBuilder();
		
		String ln;
		while ((ln = rdr.readLine()) != null) {
			bldr.append(ln).append('\n');
		}
		
		rdr.close();
		
		return bldr.toString();
	}

	@Override
	public void bind() {
		GL20.glUseProgram(getID());
	}

	@Override
	public void unbind() {
		GL20.glUseProgram(0);
	}

	@Override
	protected GLGCObject getGLGCObject() {
		return new ShaderGLGCObject(getID());
	}

}
class ShaderGLGCObject extends GLGCObject {

	public ShaderGLGCObject(int id) {
		super(id);
	}

	@Override
	public void delete() {
		GL20.glDeleteProgram(getID());
	}
	
}