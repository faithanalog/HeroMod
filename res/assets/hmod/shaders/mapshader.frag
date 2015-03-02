#version 120

uniform sampler2D map_sampler;
uniform float map_scale;

varying vec2 v_tex;

//const float pix_size = 1.0 / 352.0;
//const float pix_size2 = 2.0 / 352.0;

#define ONE_OVER_LOG_256 0.1803368801
#define PIX_SIZE 0.0028409091

void main() {
    vec4 t_pre = texture2D(map_sampler, v_tex);
    
    float depth = t_pre.w;
    
    //Depth shading
    float pix = map_scale / 352.0;
    float d_left = texture2D(map_sampler, v_tex - vec2(pix, 0.0)).w;
    float d_top  = texture2D(map_sampler, v_tex - vec2(0.0, pix)).w;
    
    float dif = (depth - d_left) + (depth - d_top);
    float lg = log(1.0 + abs(dif * 256.0)) * ONE_OVER_LOG_256 * 0.5;
    if (dif < 0.0) {
        lg = lg * -1.0;
    }
    float mult = 1.0 + lg;
    
    
    vec4 tex = vec4(t_pre.xyz * mult, 1.0);
    
    
    //Border
    float dist = distance(v_tex, vec2(0.5, 0.5)); 
    if (dist > 0.485) {
        tex = vec4(0.0, 0.0, 0.0, 0.0);
    }
    float min = 0.470;
    float max = 0.50;
    if (dist >= min && dist <= max) {
        float amnt = 1.0;
        if (dist <= 0.48) {
            amnt = 1.0 - (0.48 - dist) / 0.01;
        } else if (dist >= 0.49) {
            amnt = 1.0 - (dist - 0.49) / 0.01;
        }
        //float amnt = 1.0 - abs((dist - 0.485) / 0.015);
        float white = smoothstep(0.0, 1.0, amnt);
        if (tex.w == 0.0) {
            tex = vec4(1.0, 1.0, 1.0, white);
        } else {
            tex = (1.0 - white) * tex + vec4(white, white, white, white);
        }
    }
    gl_FragColor = tex;
}