/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{html,js,svelte,ts}'],
  theme: {
    extend: {
      colors: {
        border: 'hsl(240, 5.9%, 90%)',
        input: 'hsl(240, 5.9%, 90%)',
        ring: 'hsl(270, 50%, 60%)',
        background: 'hsl(0, 0%, 100%)',
        foreground: 'hsl(280, 10%, 18%)',
        primary: {
          DEFAULT: 'hsl(270, 50%, 60%)',
          foreground: 'hsl(0, 0%, 100%)',
        },
        secondary: {
          DEFAULT: 'hsl(24, 70%, 60%)',
          foreground: 'hsl(0, 0%, 100%)',
        },
        accent: {
          DEFAULT: 'hsl(160, 45%, 50%)',
          foreground: 'hsl(0, 0%, 100%)',
        },
        muted: {
          DEFAULT: 'hsl(240, 4%, 95%)',
          foreground: 'hsl(240, 4%, 46%)',
        },
        destructive: {
          DEFAULT: 'hsl(0, 72%, 51%)',
          foreground: 'hsl(0, 0%, 100%)',
        },
      },
    },
  },
  plugins: [],
}

