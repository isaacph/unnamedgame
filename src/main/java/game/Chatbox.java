package game;

import org.joml.Matrix4f;

import org.joml.Vector4f;
import render.BoxRenderer;
import render.Font;

import java.util.ArrayList;

public class Chatbox {

    private Font font;
    private BoxRenderer boxRender;
    private GameTime time;

    public ArrayList<String> lines;
    public StringBuffer typing;
    public int displayLines = 7;
    public float x = 0, y = 40;
    public float jump = 40;
    public float width = 800;
    public boolean focus = false;
    public static final float FOCUS_TIME = 5;
    public static final int CAPACITY = 40;
    public float focusTimer = 0;
    public float lineTimer = 0;

    public ArrayList<String> commands = new ArrayList<>();

    public Chatbox(Font f, BoxRenderer b, GameTime gameTime) {
        lines = new ArrayList<>();
        typing = new StringBuffer();
        font = f;
        boxRender = b;
        time = gameTime;
    }

    public void update() {
        if(!focus) {
            focusTimer -= time.getDelta();
            if(focusTimer < 0) {
                focusTimer = 0;
            }
        } else {
            focusTimer = FOCUS_TIME;
            lineTimer += time.getDelta();
        }
    }

    public void draw(Matrix4f ortho) {
        boxRender.draw(new Matrix4f(ortho).translate(x + width / 2, y + displayLines * jump / 2 - 30.0f, 0)
            .scale(width, displayLines * jump + 20.0f, 0), new Vector4f(0, 0, 0, 0.3f * focusTimer / FOCUS_TIME));
        float pos = y;
        for (int i = 0;
             i < displayLines - 1;
             ++i) {
            int next = lines.size() - (displayLines - 1) + i;
            if (next >= 0 && next < lines.size()) {
                font.draw(lines.get(lines.size() - (displayLines - 1) + i), x, pos, ortho, new Vector4f(1, 1, 1, focusTimer / FOCUS_TIME));
            }
            pos += jump;
        }
        if(focus) {
            if((int) (lineTimer * 4) % 2 == 0) {
                font.draw(typing.toString(), x, pos, ortho, new Vector4f(1, 1, 1, focusTimer / FOCUS_TIME));
            } else {
                font.draw(typing.toString() + "|", x, pos, ortho, new Vector4f(1, 1, 1, focusTimer / FOCUS_TIME));
            }
        }
    }

    private void add(String s) {
        if(lines.size() + 1 > CAPACITY) {
            lines.remove(0);
        }
        lines.add(s);
    }

    public void println(String s) {
        focusTimer = FOCUS_TIME;
        int index = 0, start = 0;
        while(index < s.length()) {
            float width = 0;
            while (width < this.width && index < s.length()) {
                ++index;
                width = font.textWidth(s.substring(start, index));
            }
            if(width >= this.width) --index;
            add(s.substring(start, index));
            start = index;
        }
    }

    public void enable() {
        focus = true;
        focusTimer = FOCUS_TIME;
        lineTimer = 0;
    }
    public void disable() {
        focus = false;
        focusTimer = FOCUS_TIME;
        lineTimer = 0;
    }
    public boolean send() {
        if(typing.length() == 0) return false;
        commands.add(typing.toString());
        typing.delete(0, typing.length());
        return true;
    }
}