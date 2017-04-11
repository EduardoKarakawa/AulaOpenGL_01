package br.pucpr.cg;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import br.pucpr.mage.Keyboard;
import br.pucpr.mage.Scene;
import br.pucpr.mage.Window;

public class Triangle implements Scene {
    private Keyboard keys = Keyboard.getInstance();
    private int vao;
    private int position;
    private int shader;

	private static final String	VERTEX_SHADER = "#version 330\n" +
			"in	vec2 aPosition;\n" +
			"void main(){\n" +
			"	gl_Position = vec4(aPosition, 0.0, 1.0);\n" +
			"}";

	private static final String	FRAGMENT_SHADER	= "#version	330\n" +
			"out vec4 out_color;\n" +
			"void	main(){\n" +
			"	out_color	= vec4(1.0,	1.0, 0.0, 1.0);\n" +
			"}";

	private int compileShader(int shaderType, String code){
		// Compila o shader
		int shader = glCreateShader(shaderType);
		glShaderSource(shader, code);
		glCompileShader(shader);

		// Testa pela existencia de erros
		if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE){
			throw new RuntimeException("Unable to compile shader." +
				glGetShaderInfoLog(shader));
		}

		// Retorna o shader
		return shader;
	}

	public int  linkProgram(int... shaders){
		//Cria o programa e associaos shaders
		int program = glCreateProgram();
		for(int shader : shaders){
			glAttachShader(program, shader);
		}

		// Faz o linking e testa por erros
		glLinkProgram(program);
		if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE){
			throw new RuntimeException("Unable to link shaders." +
				glGetProgramInfoLog(program)
			);
		}

		// Desassocia e exclui os shaders
		for(int shader : shaders){
			glDetachShader(program, shader);
			glDeleteShader(shader);
		}

		// Retorna o programa gerado
		return program;
	}

	@Override
	public void init() {		
		//Define a cor de limpeza da tela
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		vao = glGenVertexArrays();
		glBindVertexArray(vao);


		// Definir as posicoes dos vertex em ordem anti horario para que a face fique virada para frente
		float[] vertexData = new float[]{
				 0.0f,	 0.5f,
				-0.5f,	-0.5f,
				 0.5f,	-0.5f
		};


		// Cria um floatBuffer no java para copiar o vertex, utilizando o tamanho do mesmo
		FloatBuffer positionBuffer = BufferUtils.createFloatBuffer(vertexData.length);
		positionBuffer.put(vertexData).flip();
/*		positionBuffer.put(
			new float[]{
				0.0f,	 0.5f,
				-0.5f,	-0.5f,
				0.5f,	-0.5f
			}
		).flip();
*/

		// Passa os vertex para a placa de video
		position = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, position);
		glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		// Compila os shaders
		int vs = compileShader(GL_VERTEX_SHADER, VERTEX_SHADER);
		int fs = compileShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);

		// Linka os shaders
		shader = linkProgram(vs, fs);


		glBindVertexArray(0);
	}

	@Override
	public void update(float secs) {	
        if (keys.isPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(glfwGetCurrentContext(), GLFW_TRUE);
            return;
        }
	}

	@Override
	public void draw() {
		glClear(GL_COLOR_BUFFER_BIT);

		glUseProgram(shader);
		glBindVertexArray(vao);

		// Associa o buffer "position" ao atributo "aPosition"
		int aPosition = glGetAttribLocation(shader, "aPosition");
		glEnableVertexAttribArray(aPosition);

		glBindBuffer(GL_ARRAY_BUFFER, position);
		glVertexAttribPointer(aPosition, 2, GL_FLOAT, false, 0, 0);

		//Comanda o desenho
		glDrawArrays(GL_TRIANGLES, 0, 3);

		//Faxina
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDisableVertexAttribArray(aPosition);
		glBindVertexArray(0);
		glUseProgram(0);

	}

	@Override
	public void deinit() {
	}

	public static void main(String[] args) {
		new Window(new Triangle()).show();
	}
}
